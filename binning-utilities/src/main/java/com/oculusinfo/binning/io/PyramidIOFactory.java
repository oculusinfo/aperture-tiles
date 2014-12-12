/*
 * Copyright (c) 2014 Oculus Info Inc. http://www.oculusinfo.com/
 * 
 * Released under the MIT License.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oculusinfo.binning.io;



import java.util.List;

import com.oculusinfo.binning.io.impl.FileSystemPyramidIOFactory;
import com.oculusinfo.factory.ConfigurableFactory;
import com.oculusinfo.factory.UberFactory;
import com.oculusinfo.factory.properties.JSONProperty;



/**
 * Factory class to create the standard types of PyramidIOs
 * 
 * @author nkronenfeld
 */
public class PyramidIOFactory extends UberFactory<PyramidIO> {
	public static JSONProperty  INITIALIZATION_DATA = new JSONProperty("data",
	    "Data to be passed to the PyramidIO for read initialization",
	    null);



	public PyramidIOFactory (ConfigurableFactory<?> parent, List<String> path,
	                         List<ConfigurableFactory<? extends PyramidIO>> children) {
		this(null, parent, path, children);
	}

	public PyramidIOFactory (String name, ConfigurableFactory<?> parent,
	                         List<String> path,
	                         List<ConfigurableFactory<? extends PyramidIO>> children) {
		super(name, PyramidIO.class, parent, path, true, children, FileSystemPyramidIOFactory.NAME);

		addProperty(INITIALIZATION_DATA);
	}
}
