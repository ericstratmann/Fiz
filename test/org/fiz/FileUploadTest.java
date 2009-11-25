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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemStream;

/**
 * Junit tests for the FileUpload class.
 */

public class FileUploadTest extends junit.framework.TestCase {

    /* Constants used in FileItemFixture and FileItemStreamFixture. */
    protected static String FI_CONTENT = "FileItemFixture file contents";
    protected static String FI_CTYPE = "FileItemFixture content type";
    protected static String FI_FNAME = "FileItemFixture field name";
    protected static String FI_NAME = "FileItemFixture name";
    protected static String FIS_CONTENT = "FileItemStreamFixture file contents";
    protected static String FIS_CTYPE = "FileItemStreamFixture content type";
    protected static String FIS_FNAME = "FileItemStreamFixture field name";
    protected static String FIS_NAME = "FileItemStreamFixture name";
    
    /* An instance of each fixture class used, created each time by setUp(). */
    FileItemFixture item;
    FileItemStreamFixture itemStream;
    
    public void setUp() {
        item = new FileItemFixture();
        itemStream = new FileItemStreamFixture();
    }
    
    public void test_constructor_fileItem() {
        // FileItem constructor.
        FileUpload upload = new FileUpload(item);
        assertEquals("FileItem cached", item, upload.item);
        assertTrue("serverFileAccess permitted", upload.serverFileAccess);
        assertEquals("itemStreamBytes null", null, upload.itemStreamBytes);
    }
    
    public void test_constructor_fileItemStream() {
        // FileItemStream constructor (before another ItemStream is created).
        FileUpload upload1 = new FileUpload(itemStream);
        assertEquals("FileItemStream cached", itemStream, upload1.itemStream);
        assertFalse("serverFileAccess not permitted", upload1.serverFileAccess);
        assertTrue("itemStreamBytes filled", Arrays.equals(
                "FileItemStreamFixture file contents".getBytes(), 
                upload1.itemStreamBytes));
        
        // FileItemStream constructor (after another ItemStream is created);
        new FileItemStreamFixture();
        boolean error = false;
        try {
            new FileUpload(itemStream);
        } catch (InternalError e) {
            error = true;
        }
        assertTrue("FileItemStream constructor fails when a subsequent " +
        		"ItemStream exists", error);
    }
    
    public void test_get() {
        // FileItem instance.
        FileUpload upload1 = new FileUpload(item);
        assertTrue("FileItem get()", Arrays.equals(FI_CONTENT.getBytes(), 
                upload1.get()));
        
        // FileItemStream instance.
        FileUpload upload2 = new FileUpload(itemStream);
        assertTrue("FileItemStream get()", Arrays.equals(FIS_CONTENT.getBytes(), 
                upload2.get()));
    }
    
    public void test_getContentType() {
        // FileItem instance.
        FileUpload upload1 = new FileUpload(item);
        assertEquals("FileItem getContentType()", FI_CTYPE, 
                upload1.getContentType());
        
        // FileItemStream instance.
        FileUpload upload2 = new FileUpload(itemStream);
        assertEquals("FileItemStream getContentType()", FIS_CTYPE, 
                upload2.getContentType());
    }
    
    public void test_getFieldName() {
        // FileItem instance.
        FileUpload upload1 = new FileUpload(item);
        assertEquals("FileItem getFieldName()", FI_FNAME, 
                upload1.getFieldName());
        
        // FileItemStream instance.
        FileUpload upload2 = new FileUpload(itemStream);
        assertEquals("FileItemStream getFieldName()", FIS_FNAME, 
                upload2.getFieldName());
    }
    
    public void test_getFileItem() {
        // FileItem instance.
        FileUpload upload1 = new FileUpload(item);
        assertEquals("FileItem getFileItem()", item, upload1.getFileItem());
        
        // FileItemStream instance.
        FileUpload upload2 = new FileUpload(itemStream);
        assertEquals("FileItemStream getFileItem()", null, 
                upload2.getFileItem());
    }
    
    public void test_getFileItemStream() {
        // FileItem instance.
        FileUpload upload1 = new FileUpload(item);
        assertEquals("FileItem getFileItemStream()", null, 
                upload1.getFileItemStream());
        
        // FileItemStream instance.
        FileUpload upload2 = new FileUpload(itemStream);
        assertEquals("FileItemStream getFileItemStream()", itemStream, 
                upload2.getFileItemStream());
    }
    
    public void test_getInputStream() {
        // FileItem instance.
        FileUpload upload1 = new FileUpload(item);
        InputStream stream1 = null;
        try {
            stream1 = upload1.getInputStream();
            byte[] buffer = new byte[FI_CONTENT.length()];
            stream1.read(buffer);
            assertTrue("FileItem getInputStream() contents", 
                    Arrays.equals(FI_CONTENT.getBytes(), buffer));
        } catch (IOException e) {
            assertTrue("FileItem getInputStream() threw an error", false);
        }
        
        // FileItemStream instance.
        FileUpload upload2 = new FileUpload(itemStream);
        InputStream stream2 = null;
        try {
            stream2 = upload2.getInputStream();
            byte[] buffer = new byte[FIS_CONTENT.length()];
            stream2.read(buffer);
            assertTrue("FileItemStream getInputStream() contents", 
                    Arrays.equals(FIS_CONTENT.getBytes(), buffer));
        } catch (IOException e) {
            assertTrue("FileItemStream getInputStream() threw an error", false);
        }
    }
    
    public void test_getName() {
        // FileItem instance.
        FileUpload upload1 = new FileUpload(item);
        assertEquals("FileItem getName()", FI_NAME, upload1.getName());
        
        // FileItemStream instance.
        FileUpload upload2 = new FileUpload(itemStream);
        assertEquals("FileItemStream getName()", FIS_NAME, upload2.getName());
    }
    
    public void test_getSize() {
        // FileItem instance.
        FileUpload upload1 = new FileUpload(item);
        assertEquals("FileItem getSize()", FI_CONTENT.length(), 
                upload1.getSize());
        
        // FileItemStream instance.
        FileUpload upload2 = new FileUpload(itemStream);
        assertEquals("FileItemStream getSize()", FIS_CONTENT.length(), 
                upload2.getSize());
    }
    
    public void test_getString() {
        // FileItem instance.
        FileUpload upload1 = new FileUpload(item);
        assertEquals("FileItem getString()", FI_CONTENT, upload1.getString());
        
        // FileItemStream instance.
        FileUpload upload2 = new FileUpload(itemStream);
        assertEquals("FileItemStream getString()", FIS_CONTENT, 
                upload2.getString());
    }
    
    public void test_getString_encoding() {
        // FileItem instance.
        FileUpload upload1 = new FileUpload(item);
        try {
            assertEquals("FileItem getString(enc)", FI_CONTENT, 
                    upload1.getString("UTF8"));
            assertEquals("FileItem getString(enc)", 
                    new String(FI_CONTENT.getBytes(), "UTF16"), 
                    upload1.getString("UTF16"));
        } catch (UnsupportedEncodingException e) {
            assertTrue("Unsupported encoding", false);
        }
        
        // FileItemStream instance.
        FileUpload upload2 = new FileUpload(itemStream);
        try {
            assertEquals("FileItemStream getString(enc)", FIS_CONTENT, 
                    upload2.getString("UTF8"));
            assertEquals("FileItemStream getString(enc)", 
                    new String(FIS_CONTENT.getBytes(), "UTF16"), 
                    upload2.getString("UTF16"));
        } catch (UnsupportedEncodingException e) {
            assertTrue("Unsupported encoding", false);
        }
    }
    
    public void test_isFormField() {
        // FileItem instance.
        FileUpload upload1 = new FileUpload(item);
        assertEquals("FileItem isFormField()", false, upload1.isFormField());
        
        // FileItemStream instance.
        FileUpload upload2 = new FileUpload(itemStream);
        assertEquals("FileItemStream isFormField()", true, 
                upload2.isFormField());
    }
    
    public void test_isInMemory() {
        // FileItem instance.
        FileUpload upload1 = new FileUpload(item);
        assertEquals("FileItem isInMemory()", false, upload1.isInMemory());
        
        // FileItemStream instance.
        FileUpload upload2 = new FileUpload(itemStream);
        assertEquals("FileItemStream isInMemory()", true, 
                upload2.isInMemory());
    }
    
    public void test_isServerFileAccessPermitted() {
        // FileItem instance.
        FileUpload upload1 = new FileUpload(item);
        assertEquals("FileItem isServerFileAccessPermitted()", true, 
                upload1.isServerFileAccessPermitted());
        
        // FileItemStream instance.
        FileUpload upload2 = new FileUpload(itemStream);
        assertEquals("FileItemStream isServerFileAccessPermitted()", false, 
                upload2.isServerFileAccessPermitted());
    }
    
    public void test_streamToBytes() {
        byte[] testContents = FI_CONTENT.getBytes();
        InputStream inputStream = new ByteArrayInputStream(testContents);
        try {
            assertTrue("streamToBytes reconstructs byte[] contents", 
                    Arrays.equals(testContents, FileUpload.streamToBytes(
                            inputStream)));
        } catch (IOException e) {
            assertTrue("Couldn't read InputStream", false);
        }
    }
    
    /* A dummy FileItem used in tests of FileUpload. */
    protected static class FileItemFixture implements FileItem {
        public FileItemFixture() {}
        
        public void delete() {}
        
        public byte[] get() {
            try {
                return getString().getBytes("UTF8");
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }
        
        public String getContentType() {
            return FI_CTYPE;
        }
        
        public String getFieldName() {
            return FI_FNAME;
        }
        
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(get());
        }
        
        public String getName() {
            return FI_NAME;
        }
        
        public OutputStream getOutputStream() throws IOException {
            return null;
        }
        
        public long getSize() {
            return getString().length();
        }
        
        public String getString() {
            return FI_CONTENT;
        }
        
        public String getString(String encoding)
                throws UnsupportedEncodingException {
            return new String(get(), encoding);
        }
        
        public boolean isFormField() {
            return false;
        }
        
        public boolean isInMemory() {
            return false;
        }
        
        public void setFieldName(String arg0) {}
        
        public void setFormField(boolean arg0) {}
        
        public void write(File arg0) throws Exception {}
    }

    /* A dummy FileItemStream used in tests of FileUpload. */
    protected static class FileItemStreamFixture implements FileItemStream {
        protected int streamIndex;
        protected static int streamIndexCounter = 0;
        
        public FileItemStreamFixture() {
            streamIndex = ++streamIndexCounter;
        }
        
        public String getContentType() {
            return FIS_CTYPE;
        }
        
        public String getFieldName() {
            return FIS_FNAME;
        }
        
        public String getName() {
            return FIS_NAME;
        }
        
        public boolean isFormField() {
            return true;
        }
        
        public InputStream openStream() throws IOException {
            if (streamIndex != streamIndexCounter) {
                throw new IOException("FileItemStream is not current");
            }
            return new ByteArrayInputStream(FIS_CONTENT.getBytes("UTF8"));
        }
        
        public FileItemHeaders getHeaders() {
            return null;
        }
        
        public void setHeaders(FileItemHeaders arg0) {}
    }
}
