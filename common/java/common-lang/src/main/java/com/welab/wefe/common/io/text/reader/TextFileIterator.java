/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.io.text.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * Tool class for traversing text files, streaming reading, no memory consumption.
 *
 * @author Zane
 */
public class TextFileIterator implements Iterator<String>, Closeable {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Last read row
     */
    private String currentLine;
    /**
     * Last read line number
     */
    private long currentLineIndex = -1;
    /**
     * Byte reader
     */
    private BufferedReader bufferedReader;
    /**
     * Can I continue reading
     */
    private boolean hasNext = true;
    /**
     * Is this the last line
     */
    private boolean lastLine = false;

    private AbstractTextReader reader;

    public TextFileIterator(AbstractTextReader reader) {
        this.reader = reader;
        this.bufferedReader = reader.getBufferedReader();
        // In order to immediately know if there is another line, read ahead
        readLine();
    }

    @Override
    public boolean hasNext() {
        if (lastLine) {
            return false;
        }
        return hasNext;
    }

    @Override
    public String next() {

        // Return the previously read data row each time
        String line = currentLine;

        // Read the next line ahead to predict hasnext
        readLine();

        return line;
    }

    private void readLine() {
        try {
            currentLineIndex++;
            currentLine = bufferedReader.readLine();
            if (currentLine == null) {
                lastLine = true;
            }
        } catch (Exception e) {
            logger.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
        }
    }


    @Override
    public void remove() {
        throw new UnsupportedOperationException("Illegal operation");
    }


    //region getter/setter

    public long getCurrentLineIndex() {
        return currentLineIndex;
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }

    //endregion


}
