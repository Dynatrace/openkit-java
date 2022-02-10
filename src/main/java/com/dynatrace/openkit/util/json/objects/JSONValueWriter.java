package com.dynatrace.openkit.util.json.objects;

/**
 * Class which provides logic to write JSON value to string
 */
class JSONValueWriter {

    private final StringBuilder stringBuilder;

    /**
     * Constructor which initializes the string builder
     */
    JSONValueWriter() {
        stringBuilder = new StringBuilder();
    }

    /**
     * Appending characters for opening an array in a JSON string
     */
    void openArray() {
        stringBuilder.append("[");
    }

    /**
     * Appending characters for closing an array in a JSON string
     */
    void closeArray() {
        stringBuilder.append("]");
    }

    /**
     * Appending characters for opening an object in a JSON string
     */
    void openObject() {
        stringBuilder.append("{");
    }

    /**
     * Appending characters for closing an object in a JSON string
     */
    void closeObject() {
        stringBuilder.append("}");
    }

    /**
     * Appending characters for a key in a JSON string
     */
    void insertKey(String key) {
        stringBuilder.append("\"");
        stringBuilder.append(key);
        stringBuilder.append("\"");
    }

    /**
     * Appending characters for a string value in a JSON string
     */
    void insertStringValue(String value) {
        stringBuilder.append("\"");
        stringBuilder.append(value);
        stringBuilder.append("\"");
    }

    /**
     * Appending characters for value which is not a string in a JSON string
     */
    void insertValue(String value) {
        stringBuilder.append(value);
    }

    /**
     * Appending characters for seperating a key value pair in a JSON string
     */
    void insertKeyValueSeperator() {
        stringBuilder.append(":");
    }

    /**
     * Appending characters for seperating arrays, objects and values in a JSON string
     */
    void insertElementSeperator() {
        stringBuilder.append(",");
    }

    /**
     * Returning the whole JSON string
     */
    public String toString() {
        return stringBuilder.toString();
    }
}
