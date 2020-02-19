package org.altbeacon.Network;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.cloudant.sync.documentstore.AttachmentException;
import com.cloudant.sync.documentstore.ConflictException;
import com.cloudant.sync.documentstore.DocumentBodyFactory;
import com.cloudant.sync.documentstore.DocumentNotFoundException;
import com.cloudant.sync.documentstore.DocumentRevision;
import com.cloudant.sync.documentstore.DocumentStore;
import com.cloudant.sync.documentstore.DocumentStoreException;
import com.cloudant.sync.event.Subscribe;
import com.cloudant.sync.event.notifications.ReplicationCompleted;
import com.cloudant.sync.event.notifications.ReplicationErrored;
import com.cloudant.sync.replication.Replicator;
import com.cloudant.sync.replication.ReplicatorBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.altbeacon.WorkTracking.MainApplication;
import org.altbeacon.WorkTracking.R;
import org.altbeacon.objects.LocationTimeStamp;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * my_datastore: Storage of employees own personal logs
 * beacons_db: Storage of beacons, should always pull from the cloud before reading
 * temp_db: Storage of information requested by a manager
 */

public class DatabaseSync {
private static String TAG = "DatabaseSync";
    public static void push(HashMap<?, ?> hashMap, URI uri, String localdb, String docId){
        File path =  MainApplication.getContext().getDir("DocumentStore", Context.MODE_PRIVATE);
        try {
            Log.i(TAG, "Push started");
            DocumentStore ds = DocumentStore.getInstance(new File(path, localdb));
            DocumentRevision retrieved = ds.database().read(docId);
            retrieved.setBody(DocumentBodyFactory.create(hashMap));
            ds.database().update(retrieved);
            String string_uri = uri.toString();
            Pattern pattern = Pattern.compile("//(.*?):");
            Matcher matcher = pattern.matcher(string_uri);
            String apiKey = matcher.group(1);

            Replicator uploader = ReplicatorBuilder
                    .push()
                    .from(ds)
                    .to(uri)
                    .iamApiKey(apiKey)
                    .build();
            CountDownLatch latch = new CountDownLatch(1);
            Listener listener = new Listener(latch);
            uploader.getEventBus().register(listener);
            uploader.start();
            latch.await();
            uploader.getEventBus().unregister(listener);
            if (uploader.getState() != Replicator.State.COMPLETE) {
                System.err.println("Error replicating TO remote");
                System.err.println(listener.errors);
            } else {
                System.out.println(String.format("Replicated %d documents in %d batches",
                        listener.documentsReplicated, listener.batchesReplicated));
            }
            ds.close();
        }catch (DocumentStoreException dse) {
            System.err.println("Problem opening or accessing DocumentStore: " + dse);
        }catch (AttachmentException ae){
            System.err.println("Problem with attachment: "+ae);
        }catch (ConflictException ce){
            System.err.println("Conflict exception: "+ce);
        }catch (InterruptedException ie){
            System.err.println("Problem with latch: "+ie);
        }catch (DocumentNotFoundException de) {
            System.err.println("Document not found: " + de);
            createDocument(hashMap, uri, localdb, docId);
        }
    }

    public static DocumentStore pull(URI uri, String localdb){
        File path = MainApplication.getContext().getDir("DocumentStore", Context.MODE_PRIVATE);
        try {
            DocumentStore ds = DocumentStore.getInstance(new File(path, localdb));
            // Create a replicator that replicates changes from the remote
            // database to the local DocumentStore.
            String string_uri = uri.toString();
            Pattern pattern = Pattern.compile("//(.*?):");
            Matcher matcher = pattern.matcher(string_uri);
            String apiKey = matcher.group(1);

            Replicator replicator = ReplicatorBuilder
                    .pull()
                    .from(uri)
                    .iamApiKey(apiKey)
                    .to(ds)
                    .build();

            // Use a CountDownLatch to provide a lightweight way to wait for completion
            CountDownLatch latch = new CountDownLatch(1);
            Listener listener = new Listener(latch);
            replicator.getEventBus().register(listener);
            replicator.start();
            latch.await();
            replicator.getEventBus().unregister(listener);
            if (replicator.getState() != Replicator.State.COMPLETE) {
                System.err.println("Error replicating FROM remote");
                System.err.println(listener.errors);
            } else {
                System.out.println(String.format("Replicated %d documents in %d batches",
                        listener.documentsReplicated, listener.batchesReplicated));
            }
            return ds;
        } catch (DocumentStoreException dse) {
            System.err.println("Problem opening or accessing DocumentStore: "+dse);
        } catch (InterruptedException ie) {
            System.err.println("Problem with latch: "+ie);
        }
        return null;
    }
    public static class Listener {

        private final CountDownLatch latch;
        public List<Throwable> errors = new ArrayList<Throwable>();
        public int documentsReplicated = 0;
        public int batchesReplicated = 0;

        Listener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Subscribe
        public void complete(ReplicationCompleted event) {
            this.documentsReplicated += event.documentsReplicated;
            this.batchesReplicated += event.batchesReplicated;
            latch.countDown();
        }

        @Subscribe
        public void error(ReplicationErrored event) {
            this.errors.add(event.errorInfo);
            latch.countDown();
        }
    }


    public static void storeLogs(ArrayList<LocationTimeStamp> list, URI uri, String docId){
        HashMap<String, LocationTimeStamp> hashMap = new HashMap<>();
        for (int  i = 0;i<list.size();i++) {
            hashMap.put(Integer.toString(i), list.get(i));
        }
        push(hashMap, uri, "my_datastore", docId);
    }

    public static ArrayList<LocationTimeStamp> getLogs(String docId){
        try {
            File path = MainApplication.getContext().getDir("DocumentStore", Context.MODE_PRIVATE);
            ArrayList<LocationTimeStamp> mLocationTimestamps = new ArrayList<>();
            DocumentStore ds = DocumentStore.getInstance(new File(path, "my_datastore"));
            DocumentRevision retrieved = ds.database().read(docId);
            Map<String, Object> map = retrieved.getBody().asMap();

            if (!map.isEmpty()) {
                for (int i = 0; i < map.size(); i++) {
                    mLocationTimestamps.add(null);
                }
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    LinkedHashMap object = (LinkedHashMap) map.get(entry.getKey());
                    LocationTimeStamp lts = new LocationTimeStamp((String) object.get("location"), (String) object.get("timeStamp"), (String) object.get("duration"));
                    mLocationTimestamps.set(Integer.parseInt(entry.getKey()), lts);
                    Log.i(TAG, entry.getKey() + entry.getValue().toString());
                }
                return mLocationTimestamps;
            }
            else{
                Log.i(TAG, "No log to retrieve");
            }
        }catch (DocumentStoreException dse) {
            System.err.println("Problem opening or accessing DocumentStore: " + dse);
        }catch (DocumentNotFoundException de) {
            System.err.println("Document not found: " + de);
        }
        return new ArrayList<LocationTimeStamp>();
    }

    public static URI getCredentials(){
        URI uri;
        try {
            uri = new URI(MainApplication.getContext().getString(R.string.employee_url));
        }catch (URISyntaxException ue){
            System.err.println("Problem with URI syntax: "+ ue);
            return null;
        }
        return uri;
    }


    public static void createDocument(HashMap<?, ?> hashMap, URI uri, String localdb, String docId){
        File path = MainApplication.getContext().getDir("DocumentStore", Context.MODE_PRIVATE);
        DocumentStore ds;
        try {
            ds = DocumentStore.getInstance(new File(path, localdb));
            DocumentRevision document = new DocumentRevision(docId);
            document.setBody(DocumentBodyFactory.create(hashMap));
            DocumentRevision saved = ds.database().create(document);
            Replicator uploader = ReplicatorBuilder.push().from(ds).to(uri).build();
            CountDownLatch latch = new CountDownLatch(1);
            Listener listener = new Listener(latch);
            uploader.getEventBus().register(listener);
            uploader.start();
            latch.await();
            uploader.getEventBus().unregister(listener);
            if (uploader.getState() != Replicator.State.COMPLETE) {
                System.err.println("Error replicating TO remote");
                System.err.println(listener.errors);
            } else {
                System.out.println(String.format("Replicated %d documents in %d batches",
                        listener.documentsReplicated, listener.batchesReplicated));
            }
            ds.close();
        }catch (DocumentStoreException dse) {
            System.err.println("Problem opening or accessing DocumentStore: " + dse);
        }catch (ConflictException ce){
            System.err.println("Conflict with document id: " + ce);
        }catch (AttachmentException ae){
            System.err.println("Attachment Exception: " + ae);
        }catch (InterruptedException ie) {
            System.err.println("Problem with latch: " + ie);
        }
    }


}
