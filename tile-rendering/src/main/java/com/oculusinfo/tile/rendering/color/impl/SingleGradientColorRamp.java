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
package com.oculusinfo.tile.rendering.color.impl;

import com.oculusinfo.tile.rendering.color.FixedPoint;

import java.awt.*;
import java.util.Arrays;

/**
 * Ramps between a 'from' colour and a 'to' colour.
 * Each colour is a single RGB value that can be specified as either hex, an
 * integer, or by word. Each colour also has an alpha value that can be
 * manipulated by adding '-alpha' to the key. ex. 'from-alpha'
 * 
 * @author cregnier
 *
 */
public class SingleGradientColorRamp extends AbstractColorRamp {

	public SingleGradientColorRamp (Color from, Color to) {
		super(false,
		      Arrays.asList(new FixedPoint(0.0, from.getRed()/255.0),   new FixedPoint(1.0, to.getRed()/255.0)),
		      Arrays.asList(new FixedPoint(0.0, from.getGreen()/255.0), new FixedPoint(1.0, to.getGreen()/255.0)),
		      Arrays.asList(new FixedPoint(0.0, from.getBlue()/255.0),  new FixedPoint(1.0, to.getBlue()/255.0)),
		      Arrays.asList(new FixedPoint(0.0, from.getAlpha()/255.0), new FixedPoint(1.0, to.getAlpha()/255.0)),
		      255);
	}
}
