/*
 * Copyright (c) 2014 Oculus Info Inc.
 * http://www.oculusinfo.com/
 *
 * Released under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oculusinfo.binning.io.impl;

import com.oculusinfo.binning.TileIndex;
import com.oculusinfo.binning.io.PyramidIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Extends the PyramidSource abstract class for URL based tiles.
 *
 */
public class UrlPyramidSource extends PyramidSourceStream {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private String _extension;
	private String _host;

	public UrlPyramidSource (String host, String extension) {
		_extension = extension;
		_host = host;
	}

	@Override
	protected InputStream getSourceTileStream(String basePath, TileIndex tile) {
		String location = String.format(
			"http://" + _host + "/%s/" + PyramidIO.TILES_FOLDERNAME + "/%d/%d/%d." + _extension,
			basePath, tile.getLevel(), tile.getX(), tile.getY());
		try {
			return new URL(location).openStream();
		} catch (MalformedURLException e){
			LOGGER.error("Malformed URL supplied", e);
			return null;
		} catch (IOException e){
			return null;
		}
	}

	@Override
	protected InputStream getSourceMetaDataStream (String basePath) {
		String location = "http://" + _host + "/" + basePath + "/" + PyramidIO.METADATA_FILENAME;
		try {
			return new URL(location).openStream();
		} catch (MalformedURLException e){
			LOGGER.error("Malformed URL supplied", e);
			return null;
		} catch (IOException e){
			return null;
		}
	}

}
