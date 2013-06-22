package com.github.davidmoten.geo;

import static com.github.davidmoten.geo.GeoHash.adjacentHash;
import static com.github.davidmoten.geo.GeoHash.decodeHash;
import static com.github.davidmoten.geo.GeoHash.encodeHash;
import static com.github.davidmoten.geo.GeoHash.hashesToCoverBoundingBox;
import static com.github.davidmoten.geo.GeoHash.heightDegrees;
import static com.github.davidmoten.geo.GeoHash.instantiate;
import static com.github.davidmoten.geo.GeoHash.longitudeDiff;
import static com.github.davidmoten.geo.GeoHash.matrix;
import static com.github.davidmoten.geo.GeoHash.neighbours;
import static com.github.davidmoten.geo.GeoHash.right;
import static com.github.davidmoten.geo.GeoHash.to180;
import static com.github.davidmoten.geo.GeoHash.top;
import static com.github.davidmoten.geo.GeoHash.widthDegrees;
import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

public class GeoHashTest {

	private static final double HARTFORD_LON = -72.727175;
	private static final double HARTFORD_LAT = 41.842967;
	private static final double SCHENECTADY_LON = -73.950691;
	private static final double SCHENECTADY_LAT = 42.819581;
	private static final double PRECISION = 0.000000001;

	@Test
	public void testWhiteHouseHashEncode() {
		assertEquals("dqcjqcp84c6e",
				encodeHash(38.89710201881826, -77.03669792041183));
	}

	@Test
	public void testWhiteHouseHashDecode() {
		LatLong point = decodeHash("dqcjqcp84c6e");
		assertEquals(point.getLat(), 38.89710201881826, PRECISION);
		assertEquals(point.getLon(), -77.03669792041183, PRECISION);
	}

	@Test
	public void testFromGeoHashDotOrg() {
		assertEquals("6gkzwgjzn820", encodeHash(-25.382708, -49.265506));
	}

	@Test
	public void testHashOfNonDefaultLength() {
		assertEquals("6gkzwg", encodeHash(-25.382708, -49.265506, 6));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testHashEncodeGivenNonPositiveLength() {
		encodeHash(-25.382708, -49.265506, 0);
	}

	@Test
	public void testAnother() {
		assertEquals("sew1c2vs2q5r", encodeHash(20, 31));
	}

	@Test
	public void testAdjacentBottom() {
		assertEquals("u0zz", adjacentHash("u1pb", Direction.BOTTOM));
	}

	@Test
	public void testAdjacentTop() {
		assertEquals("u1pc", adjacentHash("u1pb", Direction.TOP));
	}

	@Test
	public void testAdjacentLeft() {
		assertEquals("u1p8", adjacentHash("u1pb", Direction.LEFT));
	}

	@Test
	public void testAdjacentRight() {
		assertEquals("u300", adjacentHash("u1pb", Direction.RIGHT));
	}

	@Test
	public void testNeighbouringHashes() {
		String center = "dqcjqc";
		Set<String> neighbours = Sets.newHashSet("dqcjqf", "dqcjqb", "dqcjr1",
				"dqcjq9", "dqcjqd", "dqcjr4", "dqcjr0", "dqcjq8");
		assertEquals(neighbours, Sets.newHashSet(neighbours(center)));
	}

	@Test
	public void testHashDecodeOnBlankString() {
		LatLong point = decodeHash("");
		assertEquals(0, point.getLat(), PRECISION);
		assertEquals(0, point.getLon(), PRECISION);
	}

	@Test
	public void testInstantiation() {
		instantiate();
	}

	@Test
	public void testSpeed() {
		long t = System.currentTimeMillis();
		int numIterations = 100000;
		for (int i = 0; i < numIterations; i++)
			encodeHash(38.89710201881826, -77.03669792041183);
		double numPerSecond = numIterations / (System.currentTimeMillis() - t)
				* 1000;
		System.out.println("num encodeHash per second=" + numPerSecond);

	}

	@Test
	public void testMovingHashCentreUpByGeoHashHeightGivesAdjacentHash() {
		String hash = "drt2";
		String top = top(hash);
		System.out.println(top(top(top(top))));
		double d = heightDegrees(hash.length());
		assertEquals(top, encodeHash(decodeHash(hash).add(d, 0), hash.length()));
		assertEquals(top,
				encodeHash(decodeHash(hash).add(d / 2 + 0.1, 0), hash.length()));
		assertEquals(hash,
				encodeHash(decodeHash(hash).add(d / 2 - 0.1, 0), hash.length()));
	}

	@Test
	public void testMovingHashCentreRightByGeoHashWidthGivesAdjacentHash() {
		String hash = "drt2";
		String right = right(hash);
		double d = widthDegrees(hash.length());
		assertEquals(right,
				encodeHash(decodeHash(hash).add(0, d), hash.length()));
		assertEquals(right,
				encodeHash(decodeHash(hash).add(0, d / 2 + 0.1), hash.length()));
		assertEquals(hash,
				encodeHash(decodeHash(hash).add(0, d / 2 - 0.1), hash.length()));
	}

	/**
	 * <p>
	 * Use this <a href=
	 * "http://www.lucenerevolution.org/sites/default/files/Lucene%20Rev%20Preso%20Smiley%20Spatial%20Search.pdf"
	 * >link</a> for double-checking.
	 * </p>
	 */
	@Test
	public void testCoverBoundingBoxAroundBoston() {

		Set<String> hashes = hashesToCoverBoundingBox(SCHENECTADY_LAT,
				SCHENECTADY_LON, HARTFORD_LAT, HARTFORD_LON, 1);

		// check schenectady hash
		assertEquals("dre7", encodeHash(SCHENECTADY_LAT, SCHENECTADY_LON, 4));
		// check hartford hash
		assertEquals("drkq", encodeHash(HARTFORD_LAT, HARTFORD_LON, 4));

		// check neighbours
		assertEquals("drs", adjacentHash("dre", Direction.RIGHT));
		assertEquals("dr7", adjacentHash("dre", Direction.BOTTOM));
		assertEquals("drk", adjacentHash("drs", Direction.BOTTOM));

		for (String hash : hashes) {
			System.out.println(decodeHash(hash) + ", hash=" + hash);
		}
		// checked qualitatively against
		//
		// assertEquals(Sets.newHashSet("dre", "dr7", "drs", "drk"), hashes);
		System.out.println(matrix("dreb", 5, hashes));
		assertEquals(Sets.newHashSet("dreq", "dr7q", "dreu", "dres", "dr7w",
				"dre6", "dre2", "drek", "drkn", "dreb", "drsh", "dref", "dred",
				"dre8", "dr7y", "drs4", "drsn", "drew", "drs0", "drey"), hashes);
	}

	@Test
	public void testCoverBoundingBoxAroundBostonNumIsTwo() {

		Set<String> hashes = hashesToCoverBoundingBox(SCHENECTADY_LAT,
				SCHENECTADY_LON, HARTFORD_LAT, HARTFORD_LON, 3);

		for (String hash : hashes) {
			System.out.println(decodeHash(hash) + ", hash=" + hash);
		}
		// checked qualitatively against
		//
		// assertEquals(Sets.newHashSet("dre", "dr7", "drs", "drk"), hashes);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCoverBoundingBoxMustBePassedMinHashesGreaterThanZero() {
		hashesToCoverBoundingBox(0, 135, 10, 145, 0);
	}

	@Test
	public void test() {
		String h = "sew1c2vs2q5r";
		for (int i = h.length(); i >= 1; i--) {
			String hash = h.substring(0, i);
			LatLong p1 = decodeHash(hash);
			LatLong p2 = decodeHash(adjacentHash(hash, Direction.RIGHT));
			LatLong p3 = decodeHash(adjacentHash(hash, Direction.BOTTOM));
			double v = Math.abs(180 / (p2.getLon() - p1.getLon()));
			double v2 = Math.abs(360 / (p3.getLat() - p1.getLat()));
			System.out.println("lon " + i + "\t" + Math.log(v) / Math.log(2));
			System.out.println("lat " + i + "\t" + Math.log(v2) / Math.log(2));
		}
	}

	@Test
	public void testTo180() {
		assertEquals(0, to180(0), PRECISION);
		assertEquals(10, to180(10), PRECISION);
		assertEquals(-10, to180(-10), PRECISION);
		assertEquals(180, to180(180), PRECISION);
		assertEquals(-180, to180(-180), PRECISION);
		assertEquals(-170, to180(190), PRECISION);
		assertEquals(170, to180(-190), PRECISION);
		assertEquals(-170, to180(190 + 360), PRECISION);
	}

	@Test
	public void testLongitudeDiff() {
		assertEquals(10, longitudeDiff(15, 5), PRECISION);
		assertEquals(10, longitudeDiff(-175, 175), PRECISION);
		assertEquals(350, longitudeDiff(175, -175), PRECISION);
	}

	@Test
	public void testGeoHashWidthDegrees() {
		encodeHash(-25.382708, -49.265506, 6);
		encodeHash(-25.382708, -49.265506, 5);
		encodeHash(-25.382708, -49.265506, 4);
		encodeHash(-25.382708, -49.265506, 3);
		encodeHash(-25.382708, -49.265506, 2);
		encodeHash(-25.382708, -49.265506, 1);
		assertEquals(45.0, widthDegrees(1), 0.00001);

		assertEquals(11.25, widthDegrees(2), 0.00001);
		assertEquals(1.40625, widthDegrees(3), 0.00001);
		assertEquals(0.3515625, widthDegrees(4), 0.00001);
		assertEquals(0.0439453125, widthDegrees(5), 0.00001);
		assertEquals(0.010986328125, widthDegrees(6), 0.00001);
	}

	@Test
	public void testGeoHashHeightDegrees() {
		encodeHash(-25.382708, -49.265506, 6);
		encodeHash(-25.382708, -49.265506, 5);
		encodeHash(-25.382708, -49.265506, 4);
		encodeHash(-25.382708, -49.265506, 3);
		encodeHash(-25.382708, -49.265506, 2);
		encodeHash(-25.382708, -49.265506, 1);
		assertEquals(45.0 / 2, heightDegrees(1), 0.00001);

		assertEquals(11.25 / 2, heightDegrees(2), 0.00001);
		assertEquals(1.40625 / 2, heightDegrees(3), 0.00001);
		assertEquals(0.3515625 / 2, heightDegrees(4), 0.00001);
		assertEquals(0.0439453125 / 2, heightDegrees(5), 0.00001);
		assertEquals(0.010986328125 / 2, heightDegrees(6), 0.00001);
	}

	@Test
	public void testMatrix() {
		System.out.println(matrix("dred", -5, -5, 5, 5));
	}
}
