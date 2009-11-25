/* Copyright (c) 2009 Stanford University
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.fiz;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemStream;

/**
 * Encapsulates a file uploaded to the Fiz application server.  Depending on 
 * whether or not the server permits file system access, the file is stored 
 * internally as either a FileItem or a FileItemStream.  For operations 
 * specific to on-disk files (delete, getOutputStream, write), the application
 * should retrieve the underlying FileItem (if it exists) and operate on it 
 * directly.
 */
public class FileUpload {
    
    /* The FileItem representing the file upload, if specified. */
    protected FileItem item = null;
    /* The FileItemStream representing the file upload, if specified. */
    protected FileItemStream itemStream = null;
    /* Whether or not the server permits filesystem access.  Equivalent to 
     * whether or not the underlying file upload is represented as a FileItem 
     * or a FileItemStream. */
    protected boolean serverFileAccess;
    /* A cached copy of the bytes of a FileItemStream. */
    protected byte[] itemStreamBytes = null;
    
    /**
     * Constructs a new FileUpload given a FileItem.
     * 
     * @param fileItem          The FileItem representation of the file.
     */
    public FileUpload(FileItem fileItem) {
        item = fileItem;
        serverFileAccess = true;
    }
    
    /**
     * Constructs a new FileUpload given a FileItemStream.  Caches the byte[] 
     * contents of the FileItemStream, for use in subsequent method calls.
     * 
     * @param fileItemStream    The FileItemStream representation of the file.
     */
    public FileUpload(FileItemStream fileItemStream) {
        itemStream = fileItemStream;
        serverFileAccess = false;
        
        // Cache the bytes from the FileItemStream now - they must be read 
        // before the next FileItemStream is read (otherwise the data is lost), 
        // and they may only be read once.
        try {
            itemStreamBytes = streamToBytes(itemStream.openStream());
        } catch (IOException e) {
            throw new InternalError("FileItemStream could not be read");
        }
    }
    
    /**
     * Gets the bytes of the file.  For FileItemStream representations, the 
     * stream is read and its contents are cached for future calls.  For 
     * FileItem representations, we take the bytes straight from the FileItem.
     * 
     * @return                  The byte[] contents of the file.
     */
    public byte[] get() {
        if (serverFileAccess) {
            return item.get();
        } else {
            return itemStreamBytes;
        }
    }
    
    /**
     * @return                  The file's content type.
     */
    public String getContentType() {
        if (serverFileAccess) {
            return item.getContentType();
        } else {
            return itemStream.getContentType();
        }
    }
    
    /**
     * @return                  The name of the field in the multipart form 
     *                          corresponding to this file.
     */
    public String getFieldName() {
        if (serverFileAccess) {
            return item.getFieldName();
        } else {
            return itemStream.getFieldName();
        }
    }
    
    /**
     * @return                  The underlying FileItem representation, or null 
     *                          if the file is represented as a FileItemStream.
     */
    public FileItem getFileItem() {
        return item;
    }
    
    /**
     * @return                  The underlying FileItemStream representation, or
     *                          null if the file is represented as a FileItem.
     */
    public FileItemStream getFileItemStream() {
        return itemStream;
    }
    
    /**
     * Gets an InputStream on the contents of the file.
     * 
     * @return                  An InputStream on the contents of the file.
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {
        if (serverFileAccess) {
            return item.getInputStream();
        } else {
            return itemStream.openStream();
        }
    }
    
    /**
     * @return                  The name of the file.
     */
    public String getName() {
        if (serverFileAccess) {
            return item.getName();
        } else {
            return itemStream.getName();
        }
    }
    
    /**
     * @return                  The size of the file, in bytes.
     */
    public long getSize() {
        if (serverFileAccess) {
            return item.getSize();
        } else {
            return itemStreamBytes.length;
        }
    }
    
    /**
     * @return                  A String representation of the file, using the 
     *                          default encoding.
     */
    public String getString() {
        if (serverFileAccess) {
            return item.getString();
        } else {
            return new String(itemStreamBytes);
        }
    }
    
    /**
     * Returns a String representation of the file, using the specified 
     * encoding.
     * 
     * @param encoding          The String encoding to use.
     * @return                  A String representation of the file.
     * @throws UnsupportedEncodingException
     */
    public String getString(String encoding) 
            throws UnsupportedEncodingException {
        if (serverFileAccess) {
            return item.getString(encoding);
        } else {
            return new String(itemStreamBytes, encoding);
        }
    }
    
    /**
     * @return                  Whether the file instance represents a simple 
     *                          form field.
     */
    public boolean isFormField() {
        if (serverFileAccess) {
            return item.isFormField();
        } else {
            return itemStream.isFormField();
        }
    }
    
    /**
     * @return                  Whether the file is being read from memory
     *                          (always true for FileItemStream instances).
     */
    public boolean isInMemory() {
        if (serverFileAccess) {
            return item.isInMemory();
        } else {
            return true;
        }
    }
    
    /**
     * @return                  Whether the server permits file system access.
     *                          Corresponds to whether the file is represented 
     *                          internally as a FileItem or a FileItemStream.
     */
    public boolean isServerFileAccessPermitted() {
        return serverFileAccess;
    }
    
    
    /**
     * Given an InputStream, returns its contents as a String.
     * 
     * @param stream                  The InputStream to read into a String.
     * @return                        The contents of the InputStream.
     * @throws IOException
     */
    protected static byte[] streamToBytes(InputStream stream) 
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int nRead;
        while ((nRead = stream.read(buffer)) != -1) {
            baos.write(buffer, 0, nRead);
        }
        baos.close();
        return baos.toByteArray();
    }
}
