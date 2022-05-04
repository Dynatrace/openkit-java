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
        stringBuilder.append(escapeString(key));
        stringBuilder.append("\"");
    }

    /**
     * Appending characters for a string value in a JSON string
     */
    void insertStringValue(String value) {
        stringBuilder.append("\"");
        stringBuilder.append(escapeString(value));
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

    /**
     * Escaping the string used in JSON obj
     * @param value string value which should be escaped
     * @return Escaped string value
     */
    private String escapeString(String value)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < value.length(); i++)
        {
            switch (value.charAt(i))
            {
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '/': sb.append("\\/"); break;
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                default:
                    if (value.charAt(i) <= 0x1f)
                    {
                        sb.append("\\u");
                        final String hex = "000" + Integer.toHexString(value.charAt(i));
                        sb.append(hex.substring(hex.length() - 4));
                    }
                    else
                    {
                        sb.append(value.charAt(i));
                    }
                    break;
            }
        }

        return sb.toString();
    }
}
