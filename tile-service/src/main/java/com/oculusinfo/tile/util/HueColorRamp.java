package com.oculusinfo.tile.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HueColorRamp implements ColorRamp {

	private static final Logger logger = LoggerFactory.getLogger(HueColorRamp.class);

	private double fromVal = 0.0;
	private double toVal = 0.0;
	
	private final double clamp(double v, double min, double max) {
		return (v > min)? ((v < max)? v : max): min;   
	}
	
	public HueColorRamp(ColorRampParameter params) {
		try {
			fromVal = clamp(getNumber(params.getString("from")).doubleValue(), 0, 1);
		}
		catch (Exception e) {
			logger.error("Hue ramp's 'from' value is invalid. Should be 0 -> 1", e);
			fromVal = 0;
		}
		try {
			toVal = clamp(getNumber(params.getString("to")).doubleValue(), 0, 1);
		}
		catch (Exception e) {
			logger.error("Hue ramp's 'to' value is invalid. Should be 0 -> 1", e);
			toVal = 0;
		}
	
	}
	
	/**
	 * Converts an object into a number.
	 * @return
	 * 	If the object is already a number then it just casts it.
	 * 	If the object is a string, then it parses it as a double.
	 * 	Otherwise the number returned is 0. 
	 */
	protected static Number getNumber(Object o) {
		Number val = 0;
		if (o instanceof Number) {
			val = (Number)o;
		}
		else if (o instanceof String) {
			val = Double.valueOf((String)o);
		}
		return val;
	}

	@Override
	public int getRGB(double scale) {
		return hslToRGB((toVal - fromVal) * scale + fromVal, 1.0, 0.5);
	}

	protected double hueToRGB(double p, double q, double t) {
		if (t < 0) t += 1;
		if (t > 1) t -= 1;
		if (t < 0.1666667) return p + (q - p) * 6 * t;
		if (t < 0.5) return q;
		if (t < 0.6666667) return p + (q - p) * (0.66666667 - t) * 6;
		return p;
	}
	
	protected int hslToRGB(double h, double s, double l) {
		double r, g, b;
		if (s != 0) {
			double q = (l < 0.5)? l * (1 + s) : l + s - l * s;
			double p = 2 * l - q;
			r = hueToRGB(p, q, h + 0.333333334);
			g = hueToRGB(p, q, h);
			b = hueToRGB(p, q, h - 0.333333334);
		}
		else {
			r = g = b = 1;
		}
		
		int ir = (int)(r * 255);
		int ig = (int)(g * 255);
		int ib = (int)(b * 255);
		
		return (0xff << 24) | (ir << 16) | (ig << 8) | ib;
	}
}
