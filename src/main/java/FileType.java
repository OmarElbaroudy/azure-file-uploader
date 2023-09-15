public enum FileType {
    TEXT(".txt"),
    CSV(".csv"),
    JSON(".json"),
    PARQUET(".hdfs");

    private final String value;

    FileType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getValue();
    }
}
