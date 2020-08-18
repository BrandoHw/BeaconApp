package org.altbeacon.Retrofit;

public class SqlResponse {

    private class done {
        private int affectedRows;
        private int changedRows;
        private int fieldCount;
        private int insertId;
        private int message;
        private boolean protocol41;
        private int serverStatus;
        private int warningCount;

        public int getAffectedRows() {
            return affectedRows;
        }

        public int getChangedRows() {
            return changedRows;
        }

        public int getFieldCount() {
            return fieldCount;
        }

        public int getInsertId() {
            return insertId;
        }

        public int getMessage() {
            return message;
        }

        public boolean isProtocol41() {
            return protocol41;
        }

        public int getServerStatus() {
            return serverStatus;
        }

        public int getWarningCount() {
            return warningCount;
        }
    }
}
