# Azure File Uploader
This Java program efficiently uploads large files to Azure blobs using concurrency.

## Description
This program follows these steps to upload files:

1. It locates the file specified by the given file path and calculates its size.
2. The file is segmented into equal-sized segments.
3. Each segment is associated with a specific offset and read from the local file system.
4. The program concurrently reads and uploads segments to Azure.

This approach ensures efficient and parallelized file uploads to Azure storage.
