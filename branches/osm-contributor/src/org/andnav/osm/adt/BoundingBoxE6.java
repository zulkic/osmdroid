// Created by plusminus on 19:06:38 - 25.09.2008
package org.andnav.osm.adt;

import static org.andnav.osm.util.MyMath.gudermann;
import static org.andnav.osm.util.MyMath.gudermannInverse;

import java.util.ArrayList;

import org.andnav.osm.util.constants.OSMConstants;
import org.andnav.osm.views.util.constants.OSMMapViewConstants;

/**
 * 
 * @author Nicolas Gramlich
 *
 */
public class BoundingBoxE6 implements OSMMapViewConstants, OSMConstants {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected final int mLatNorthE6;
	protected final int mLatSouthE6;
	protected final int mLonEastE6;
	protected final int mLonWestE6;  
	private GeoLine mNorthGeoLine;
	private GeoLine mSouthGeoLine;
	private GeoLine mWestGeoLine;
	private GeoLine mEastGeoLine;
	private GeoPoint mCenterGeoPoint;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	
	public BoundingBoxE6(final int northE6, final int eastE6, final int southE6, final int westE6){
		this.mLatNorthE6 = northE6;
		this.mLatSouthE6 = southE6;
		this.mLonWestE6 = westE6;
		this.mLonEastE6 = eastE6;
	}
	
	public BoundingBoxE6(final double north, final double east, final double south, final double west){
		this.mLatNorthE6 = (int)(north * 1E6);
		this.mLatSouthE6 = (int)(south * 1E6);
		this.mLonWestE6 = (int)(west * 1E6);
		this.mLonEastE6 = (int)(east * 1E6);
	}
	
	public static BoundingBoxE6 fromGeoPoints(final ArrayList<? extends GeoPoint> partialPolyLine) {
		int minLat = Integer.MAX_VALUE;
		int minLon = Integer.MAX_VALUE;
		int maxLat = Integer.MIN_VALUE;
		int maxLon = Integer.MIN_VALUE;
		for (GeoPoint gp : partialPolyLine) {
			final int latitudeE6 = gp.getLatitudeE6();
			final int longitudeE6 = gp.getLongitudeE6();
			
			minLat = Math.min(minLat, latitudeE6);
			minLon = Math.min(minLon, longitudeE6);
			maxLat = Math.max(maxLat, latitudeE6);
			maxLon = Math.max(maxLon, longitudeE6);
		}
		
		return new BoundingBoxE6(minLat, maxLon, maxLat, minLon);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	
	
	/**
	 * @return GeoPoint center of this BoundingBox
	 */
	public GeoPoint getCenter(){
		if(this.mCenterGeoPoint == null)
			this.mCenterGeoPoint = new GeoPoint((this.mLatNorthE6 + this.mLatSouthE6) / 2, (this.mLonEastE6 + this.mLonWestE6) / 2);
		
		return this.mCenterGeoPoint;
	}
	
	/**
	 * @return GeoLine from NorthWest to NorthEast
	 */
	public GeoLine getNorthLine(){
		if(this.mNorthGeoLine == null)
			this.mNorthGeoLine = new GeoLine(mLatNorthE6, mLonWestE6, mLatNorthE6, mLonEastE6);
		return this.mNorthGeoLine;
	}

	/**
	 * @return GeoLine from SouthEast to SouthWest
	 */
	public GeoLine getSouthGeoLine(){
		if(this.mSouthGeoLine == null)
			this.mSouthGeoLine = new GeoLine(mLatSouthE6, mLonEastE6, mLatSouthE6, mLonWestE6);
		return this.mSouthGeoLine;
	}

	/**
	 * @return GeoLine from NorthEast to SouthEast
	 */
	public GeoLine getEastGeoLine(){
		if(this.mEastGeoLine == null)
			this.mEastGeoLine = new GeoLine(mLatNorthE6, mLonEastE6, mLatSouthE6, mLonEastE6);
		return this.mEastGeoLine;
	}

	/**
	 * @return GeoLine from SouthWest to NorthWest
	 */
	public GeoLine getWestGeoLine(){
		if(this.mWestGeoLine == null)
			this.mWestGeoLine = new GeoLine(mLatSouthE6, mLonWestE6, mLatNorthE6, mLonWestE6);
		return this.mWestGeoLine;
	}
	
	public int getDiagonalLengthInMeters() {
		return new GeoPoint(this.mLatNorthE6, this.mLonWestE6).distanceTo(new GeoPoint(this.mLatSouthE6, this.mLonEastE6));
	}
	
	public int getLatNorthE6() {
		return this.mLatNorthE6;
	}
	
	public int getLatSouthE6() {
		return this.mLatSouthE6;
	}
	
	public int getLonEastE6() {
		return this.mLonEastE6;
	}
	
	public int getLonWestE6() {
		return this.mLonWestE6;
	}

	public int getLatitudeSpanE6() {
		return Math.abs(this.mLatNorthE6 - this.mLatSouthE6);
	}
	
	public int getLongitudeSpanE6() {
		return Math.abs(this.mLonEastE6 - this.mLonWestE6);
	}
	/**
	 * 
	 * @param aLatitude
	 * @param aLongitude
	 * @param reuse
	 * @return relative position determined from the upper left corner.<br />
	 * {0,0} would be the upper left corner.
	 * {1,1} would be the lower right corner.
	 * {1,0} would be the lower left corner.
	 * {0,1} would be the upper right corner. 
	 */
	public float[] getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(final int aLatitude, final int aLongitude, final float[] reuse){
		float[] out = (reuse != null) ? reuse : new float[2];
		out[MAPTILE_LATITUDE_INDEX] = ((float)(this.mLatNorthE6 - aLatitude) / getLatitudeSpanE6());
		out[MAPTILE_LONGITUDE_INDEX] = 1 - ((float)(this.mLonEastE6 - aLongitude) / getLongitudeSpanE6());
		return out;
	}
	
	public float[] getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(final int aLatitudeE6, final int aLongitudeE6, final float[] reuse){
		float[] out = (reuse != null) ? reuse : new float[2];
		out[MAPTILE_LATITUDE_INDEX] = (float)((gudermannInverse(this.mLatNorthE6 / 1E6) - gudermannInverse(aLatitudeE6 / 1E6)) / (gudermannInverse(this.mLatNorthE6 / 1E6) - gudermannInverse(this.mLatSouthE6 / 1E6)));
		out[MAPTILE_LONGITUDE_INDEX] = 1 - ((float)(this.mLonEastE6 - aLongitudeE6) / getLongitudeSpanE6());
		return out;
	}
	
	public GeoPoint getGeoPointOfRelativePositionWithLinearInterpolation(final float relX, final float relY) {		
		
		int lat = (int)(this.mLatNorthE6 - (this.getLatitudeSpanE6() * relY));
							
		int lon = (int)(this.mLonWestE6 + (this.getLongitudeSpanE6() * relX));
		
		/* Bring into bounds. */
		while(lat > 90500000)
			lat -= 90500000;
		while(lat < -90500000)
			lat += 90500000;
		
		/* Bring into bounds. */
		while(lon > 180000000)
			lon -= 180000000;
		while(lon < -180000000)
			lon += 180000000;
		
		return new GeoPoint(lat, lon);
	}
	
	public GeoPoint getGeoPointOfRelativePositionWithExactGudermannInterpolation(final float relX, final float relY) {		
		
		final double gudNorth = gudermannInverse(this.mLatNorthE6 / 1E6);
		final double gudSouth = gudermannInverse(this.mLatSouthE6 / 1E6);
		final double latD = gudermann((gudSouth + (1-relY) * (gudNorth - gudSouth)));
		int lat = (int)(latD * 1E6);
							
		int lon = (int)((this.mLonWestE6 + (this.getLongitudeSpanE6() * relX)));
		
		/* Bring into bounds. */
		while(lat > 90500000)
			lat -= 90500000;
		while(lat < -90500000)
			lat += 90500000;
		
		/* Bring into bounds. */
		while(lon > 180000000)
			lon -= 180000000;
		while(lon < -180000000)
			lon += 180000000;
		
		return new GeoPoint(lat, lon);
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================
	
	@Override
	public String toString(){
		return new StringBuffer()
			.append("N:").append(this.mLatNorthE6)
			.append("; E:").append(this.mLonEastE6)
			.append("; S:").append(this.mLatSouthE6)
			.append("; W:").append(this.mLonWestE6)
			.toString();
	}

	// ===========================================================
	// Methods
	// ===========================================================
	
	public boolean intersectsLine(final GeoPoint gpA, final GeoPoint gpB){		
		return intersectsLine(new GeoLine(gpA, gpB));
	}
	
	public boolean intersectsLine(final GeoLine pGeoLine){		
		return getNorthLine().intersects(pGeoLine)
			|| getSouthGeoLine().intersects(pGeoLine)
			|| getWestGeoLine().intersects(pGeoLine)
			|| getEastGeoLine().intersects(pGeoLine);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

