package km1930.serialport.sample.mainboard;

import java.io.Serializable;

/**
 * Created by djf on 2017/6/27.
 */

public class BoardRequestInfo implements Serializable{

    /**
     * code : 10000
     * message : 成功
     * data : {"hardCode":1,"softCode":2,"softType":1,"downloadUrl":"http://115.29.198
     * .179/apk/master/A.bin","md5":"a446a09a9c2b862b8c4f2e030007673a"}
     */

    private int code;
    private String message;
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BoardRequestInfo{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

    public static class DataBean implements Serializable{
        /**
         * hardCode : 1
         * softCode : 2
         * softType : 1
         * downloadUrl : http://115.29.198.179/apk/master/A.bin
         * md5 : a446a09a9c2b862b8c4f2e030007673a
         */

        private int hardCode;
        private int softCode;
        private int softType;
        private String downloadUrl;
        private String md5;

        public int getHardCode() {
            return hardCode;
        }

        public void setHardCode(int hardCode) {
            this.hardCode = hardCode;
        }

        public int getSoftCode() {
            return softCode;
        }

        public void setSoftCode(int softCode) {
            this.softCode = softCode;
        }

        public int getSoftType() {
            return softType;
        }

        public void setSoftType(int softType) {
            this.softType = softType;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "hardCode=" + hardCode +
                    ", softCode=" + softCode +
                    ", softType=" + softType +
                    ", downloadUrl='" + downloadUrl + '\'' +
                    ", md5='" + md5 + '\'' +
                    '}';
        }
    }
}
