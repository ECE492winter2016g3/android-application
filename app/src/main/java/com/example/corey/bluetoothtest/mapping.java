package com.example.corey.bluetoothtest;

import android.util.Log;

import java.util.ArrayList;
import java.lang.Math;

public class mapping {
	public static float PI = 3.1415926535f;

	/* Broad phase is a 10x10 grid of 100x100 cm squares */
	public static int BROAD_STEPS = 10;
	public static float BROAD_SIZE = 100.0f;

	/* Variance in the lidar range from the true value is +- 1cm */
	public static float LIDAR_VARIANCE = 4.0f;

	/* Max number of samples in a sweep by the sensor */
	public static int SWEEP_COUNT = 180;

	/* Maximum amount the robot can turn by during linear movement (1 / 30th turn | 12 deg) */
	public static float TURN_TWEAK = ((float)Math.PI * 2.0f / 30.0f);

	public static class Vec {
		public static Vec fromAngle(float theta) {
			return new Vec((float)Math.cos(theta), (float)Math.sin(theta));
		}
		public Vec(float a, float b) {
			x = a;
			y = b;
		}
		public Vec() {
			this(0, 0);
		}
		public float x;
		public float y;
		public float length() {
			return (float)Math.sqrt(x*x + y*y);
		}
		public void normalize() {
			float len = length();
			x /= len;
			y /= len;
		}
		public void negate() {
			x = -x;
			y = -y;
		}
		public void add(Vec v) {
			x += v.x;
			y += v.y;
		}
		public void sub(Vec v) {
			x -= v.x;
			y -= v.y;
		}
		public Vec copy() {
			return new Vec(x, y);
		}
	}

	public static Vec sub(Vec a, Vec b) {
		return new Vec(a.x - b.x, a.y - b.y);
	}
	public static Vec add(Vec a, Vec b) {
		return new Vec(a.x + b.x, a.y + b.y);
	}

	public static class MapPoint {
		public MapPoint() {
			position = new Vec();
		}
		public Vec position;
	}

	public static class MapSegment {
		public MapSegment() {
			pointList = new ArrayList<MapPoint>();
			origin = new Vec();
			vec = new Vec();
		}
		public ArrayList<MapPoint> pointList;
		public Vec origin;
		public Vec vec;
	}
	/* Dot product */
	float dot(Vec a, Vec b) {
		return a.x*b.x + a.y*b.y;
	}

	float cross(Vec a, Vec b) {
		return a.x*b.y - a.y*b.x;
	}
	float length(float a, float b) {
		return (float)Math.sqrt(a*a + b*b);
	}
	Vec mul(Vec a, float b) {
		return new Vec(a.x*b, a.y*b);
	}
	Vec mul(float a, Vec b) {
		return new Vec(a*b.x, a*b.y);
	}

	/* Angle between */
	float angle_between(Vec a, Vec b) {
		float frac = dot(a, b) / (a.length() * b.length());
		// Correct floating point errors making the value slightly out of range
		if (frac > 1) frac = 1;
		if (frac < -1) frac = -1;
		return (float)Math.acos(frac);
	}
	float absangle_between(Vec a, Vec b) {
		return Math.abs(angle_between(a, b));
	}

	/* Update a line segment's linear regression using it's points */
	void regressSegment(MapSegment seg) {
		// TODO: Use protections onto the line of best fit in
		// order to get a more accurate length
		MapPoint first = seg.pointList.get(0);
		MapPoint last = seg.pointList.get(seg.pointList.size()-1);

		// Linear least squares
		if (Math.abs(last.position.y - first.position.y) < Math.abs(last.position.x - first.position.x)) {
			// Linear least squares on Ys as a function of Xs
			int count = 0;
			float sumx = 0, sumy = 0, sumxy = 0, sumxx = 0;
			float greatestX = -100000;
			float smallestX = 100000;
			for (MapPoint p: seg.pointList) {
				++count;
				sumx += p.position.x;
				sumy += p.position.y;
				sumxy += p.position.x * p.position.y;
				sumxx += p.position.x * p.position.x;
				greatestX = Math.max(greatestX, p.position.x);
				smallestX = Math.min(smallestX, p.position.x);
			}
			float slope = (sumx*sumy - count*sumxy) / (sumx*sumx - count*sumxx);
			float interc = (sumy - slope*sumx) / count;
			// Now Y = interc + X*slope
			seg.origin.x = smallestX;
			seg.origin.y = interc + smallestX*slope;
			seg.vec.x = (greatestX - smallestX);
			seg.vec.y = (greatestX - smallestX)*slope;
		} else {
			// Linear least squares on Xs as a function of Ys
			int count = 0;
			float sumx = 0, sumy = 0, sumxy = 0, sumxx = 0;
			float greatestY = -100000;
			float smallestY = 100000;
			for (MapPoint p: seg.pointList) {
				++count;
				sumx += p.position.y;
				sumy += p.position.x;
				sumxy += p.position.y * p.position.x;
				sumxx += p.position.y * p.position.y;
				greatestY = Math.max(greatestY, p.position.y);
				smallestY = Math.min(smallestY, p.position.y);
			}
			float slope = (sumx*sumy - count*sumxy) / (sumx*sumx - count*sumxx);
			float interc = (sumy - slope*sumx) / count;
			// Now  X = interc + Y*slope
			seg.origin.y = smallestY;
			seg.origin.x = interc + smallestY*slope;
			seg.vec.y = (greatestY - smallestY);
			seg.vec.x = (greatestY - smallestY)*slope;
		}
	}

	/* Move points from one segment to a consecutive one near a corner */
	void balanceSegmentCorner(MapSegment a, MapSegment b) {
		boolean didMovePoint = false; // Until contested
		for (int i = 0; i < a.pointList.size(); ++i) {
			MapPoint point = a.pointList.get(i);
			Vec relpt = sub(point.position, b.origin);
			float len = b.vec.length();
			float projLenB = dot(relpt, b.vec) / len;
			float perpDistB = sub(relpt, mul(projLenB / len, b.vec)).length();
			if (projLenB > -LIDAR_VARIANCE && projLenB < len+LIDAR_VARIANCE) {
				// Can move onto B, but is it closer?
				relpt = sub(point.position, a.origin);
				len = a.vec.length();
				float projLenA = dot(relpt, a.vec) / len;
				float perpDistA = sub(relpt, mul(projLenA / len, a.vec)).length();
				// Choose
				if (perpDistB < perpDistA) {
					b.pointList.add(point);
					a.pointList.remove(i);
					--i;
					didMovePoint = true;
				}
			}
		}
		if (didMovePoint) {
			regressSegment(a);
			regressSegment(b);
		}
	}

	/* Do feature extraction */
	void featureExtract(
			ArrayList<MapPoint> points, ArrayList<MapSegment> segments,
			float angles[], float distances[])
	{
		// First, extract the global X/Y coordinates
		ArrayList<Vec> pts = new ArrayList<Vec>();
		for (int i = 0; i < angles.length; ++i) {
			Vec v = mul(distances[i], Vec.fromAngle(angles[i] + robotAngle));
			v.add(robotPosition);
			pts.add(v);
		}

		// Ready the output
		points.clear();
		segments.clear();

		// Now, starting from the first point, try to generate line segments
		int start_index = 0; // First index in the line segment
		boolean start_is_in_segment = false; // First index was already used in a segment
		float lastDifference = sub(pts.get(0), pts.get(1)).length();
		for (int i = 1; i <= angles.length; ++i) {
			//std::cout << "Point " << i << " from start " << start_index << "\n";
			// Use this point as the end point, starting at the first index, and
			// see if all the other points are near the line segment (use
			// perpendicular distance to the segment)
			float thisDifference = 0.0f;
			if (i < angles.length) {
				thisDifference = sub(pts.get(i), pts.get(i-1)).length();
			}
			boolean failed;
			if (i == angles.length) {
				// Final iteration, always fail
				failed = true;
				// } else if (thisDifference > 1.6*lastDifference
				// 	|| thisDifference < 0.625*lastDifference) {
				// 	// Not following a smooth wall
				// 	failed = true;
			} else if (thisDifference > 2*LIDAR_VARIANCE) {
				// Points too spread out
				failed = true;
			} else {
				// Normal iteration behavior
				Vec u = sub(pts.get(i), pts.get(start_index));
				u.normalize();
				Vec t = sub(pts.get(i), robotPosition); // Direction from robot to edge
				if (absangle_between(u, t) < 0.3) {
					// Line seg is at too great an angle to us, don't use it
					failed = true;
				} else {
					failed = false;
					// See if the line segment is properly ordered
					for (int j = start_index + 1; j < i; ++j) {
						Vec d = sub(pts.get(j), pts.get(start_index));
						float projLen = dot(u, d);
						d.sub(mul(projLen, u));
						// 1.5 -> allow plus variance on both endpoints
						// and minus variance on inner points to still register
						// as a segment.
						//std::cout << "length: " << length(dx, dy) << "\n";
						if (d.length() > 3.0f * LIDAR_VARIANCE) {
							failed = true;
							break;
						}
					}
				}
			}
			lastDifference = thisDifference;

			// On a fail, if we have a line segment of >3 points already, emit it
			// Otherwise, emit the start_index point and advance start_index.
			if (failed) {
				if ((i - start_index) > 2) {
					// Emit the points start_index up to i - 1
					// (i is the index that we failed to extend to)
					MapSegment seg = new MapSegment();
					for (int j = start_index; j < i; ++j) {
						MapPoint point = new MapPoint();
						point.position = pts.get(j).copy();
						seg.pointList.add(point);
					}
					regressSegment(seg);
					// Balance the segments
					if (segments.size() > 1)
						balanceSegmentCorner(segments.get(segments.size()-2), segments.get(segments.size()-1));
					segments.add(seg);

					// New start index is i *minus one*, because the end point
					// of one segment can also be the start point of a new one
					start_index = i - 1;
					start_is_in_segment = true;
				} else if (start_is_in_segment) {
					// Just advance, that point does not need to be emitted as it
					// is already in a line segment
					++start_index;
					start_is_in_segment = false;
				} else {
					// Emit the start_index point individually and advance the
					// start index by one.
					MapPoint pt = new MapPoint();
					pt.position = pts.get(start_index).copy();
					points.add(pt);

					// New start index one ahead
					++start_index;
					// start_is_in_segment = 0; -> already implied branch's conditions
				}
			} else {
				// Nothing to do, just keep extending the current line segment
			}
		}

		// Balance the last / first seg
		if (segments.size() > 2)
			balanceSegmentCorner(segments.get(segments.size()-1), segments.get(0));
	}


	// Delete features
	void deleteFeatures(ArrayList<MapPoint> points, ArrayList<MapSegment> segments) {
		// nothing to do
	}


	// Update a set of features based on a change in position / rotation of the robot
	// (Subtract, rotate, add)
	void updateFeatures(Vec oldPos, float oldTheta,
						ArrayList<MapPoint> points, ArrayList<MapSegment> segments)
	{
		// Change
		float dTheta = robotAngle - oldTheta;
		float st = (float)Math.sin(dTheta);
		float ct = (float)Math.cos(dTheta);
		float oldX = oldPos.x;
		float oldY = oldPos.y;

		// Subtract
		for (MapSegment seg: segments) {
			for (MapPoint point: seg.pointList) {
				point.position.x -= oldX;
				point.position.y -= oldY;
			}
			seg.origin.x -= oldX;
			seg.origin.y -= oldY;
		}
		for (MapPoint point: points) {
			point.position.x -= oldX;
			point.position.y -= oldY;
		}

		// Rotate
		for (MapSegment seg: segments) {
			for (MapPoint point: seg.pointList) {
				float x = point.position.x;
				float y = point.position.y;
				point.position.x = x*ct - y*st;
				point.position.y = x*st + y*ct;
			}
			float ox = seg.origin.x;
			float oy = seg.origin.y;
			seg.origin.x = ox*ct - oy*st;
			seg.origin.y = ox*st + oy*ct;
			float vx = seg.vec.x;
			float vy = seg.vec.y;
			seg.vec.x = vx*ct - vy*st;
			seg.vec.y = vx*st + vy*ct;
		}
		for (MapPoint point: points) {
			float x = point.position.x;
			float y = point.position.y;
			point.position.x = x*ct - y*st;
			point.position.y = x*st + y*ct;
		}

		// Add
		for (MapSegment seg: segments) {
			for (MapPoint point: seg.pointList) {
				point.position.x += robotPosition.x;
				point.position.y += robotPosition.y;
			}
			seg.origin.x += robotPosition.x;
			seg.origin.y += robotPosition.y;
		}
		for (MapPoint point: points) {
			point.position.x += robotPosition.x;
			point.position.y += robotPosition.y;
		}
	}


	// Tweak the rotation of the robot by a small amount to best match
	float rotationTweak(ArrayList<MapSegment> segments) {
		// For each segment in the list, try to find a segment in the map that
		// has a very similar rotation, and is somewhat nearby.

		Vec tot = new Vec(); // Total correction to make to the robot unit vector
		float totcount = 0;

		// Longest seg length squared
		float longestSegLength = 0;
		for (MapSegment to_match: segments) {
			longestSegLength = Math.max(longestSegLength, to_match.vec.length());
		}
		longestSegLength = longestSegLength*longestSegLength;

		for (MapSegment to_match: segments) {
			// Find the most similar length segment within the turn tweak
			MapSegment best_match = null;
			float best_length_diff = 10000;
			for (MapSegment a_match: allSegments) {
				float angle = absangle_between(a_match.vec, to_match.vec);
				float lengthDiff = Math.abs(a_match.vec.length() - best_length_diff);
				if (angle < TURN_TWEAK && lengthDiff < best_length_diff) {
					best_length_diff = lengthDiff;
					best_match = a_match;
				}
			}

			// If there are none within, try for double the turn tweak
			if (best_match == null) {
				for (MapSegment a_match: allSegments) {
					float angle = absangle_between(a_match.vec, to_match.vec);
					float lengthDiff = Math.abs(a_match.vec.length() - best_length_diff);
					if (angle < 2*TURN_TWEAK && lengthDiff < best_length_diff) {
						best_length_diff = lengthDiff;
						best_match = a_match;
					}
				}
			}

			// Add the weighted result to the total turn tweak
			// Let <a,b> = unit vector to turn towards
			if (best_match != null) {
				Vec a = best_match.vec.copy();
				if (dot(best_match.vec, to_match.vec) < 0) {
					a.negate();
				}
				a.normalize();

				// Let <x,y> = to match's unit direction
				Vec b = to_match.vec.copy();
				b.normalize();

				// Weight based on length
				float len = to_match.vec.length();
				len *= len; // squared
				float weight = len / longestSegLength;

				// Add the difference to the modifier
				tot.add(mul(weight, sub(a, b)));
				totcount += weight;
			}
		}

		// Modify the robot rotation
		float oldAngle = robotAngle;
		Vec u = Vec.fromAngle(robotAngle);
		u.normalize();
		u.add(mul(tot, 1.0f/totcount));
		robotAngle = (float)Math.atan2(u.y, u.x);
		Log.i("Mapping", "RotationTweak by: " + (robotAngle - oldAngle));
		return (robotAngle - oldAngle);
	}

	// hist -> histogram
	private class HistEntry {
		public float weight;
		public float diff;
		public float total;
		public int count;
	}

/*
	int linearMotionProcess(ArrayList<Vec> points) {
		return 0;
	}
*/

	// Process linear motion (post rotation tweaking)
	// Returns: 0 - success
	//          -1 - couldn't reckon position, don't add geometry
	int linearmotion_process(ArrayList<MapSegment> segments, float tweakedRot) {
		// Histogram of differences
		ArrayList<HistEntry> histogram = new ArrayList<HistEntry>();
		int hist_cap = 10;

		// Dir of movement
		Vec u = Vec.fromAngle(robotAngle);

		// Weight based on the longest segment
		float longestSegLength = 0;
		for (MapSegment to_match: segments) {
			float len = to_match.vec.length();
			longestSegLength = Math.max(longestSegLength, len);
		}

		for (MapSegment to_match: segments) {
			// Try to match to a segment with a very similar rotation, and
			// a small linear difference along the direction of supposed motion
			// (the current theta)
			for (MapSegment a_match: allSegments) {
				if (absangle_between(to_match.vec, a_match.vec) < TURN_TWEAK) {
					// Is a candidate, compute the distance to it
					// First compute the perpendicular vector from segment to match
					// = (match.o - seg.o) projected onto (perp seg.v, in the direction of movement)
					Vec d = sub(a_match.origin, to_match.origin);
					Vec perp = new Vec(-to_match.vec.y, to_match.vec.x);

					// Make sure perp is in the direction of movement
					if (dot(perp, u) < 0)
						perp.negate();

					perp.normalize();
					// Do the projection
					float perplen = dot(d, perp);
					// Now project the direction of movement onto the perpendicular vector
					float frac = dot(perp, u);
					// Now scale that up for the final movement along the direction of travel
					float dirlen = perplen / frac;

					// Weight based on length and frac
					float weight = frac * (to_match.vec.length() / longestSegLength);

					// Note: When frac is small, dirlen will be large, and errors will be
					// scaled up, so use frac as the weight of a measurement
					// Add the dirlen to the histogram
					if (frac > 0.2) {
						// 0.2 -> threshold of useful data
						// below that the wall is almost paralell to our movement
						// and we can't get a distance delta from it
						boolean added = false;
						int leastWeightIndex = 0;
						float leastWeight = 10000;
						for (int i = 0; i < histogram.size(); ++i) {
							HistEntry entry = histogram.get(i);
							if (Math.abs(entry.diff - dirlen) < 3*LIDAR_VARIANCE) {
								entry.weight += weight;
								entry.count++;
								entry.total += dirlen;
								added = true;
								break;
							}
							if (entry.weight < leastWeight) {
								leastWeight = entry.weight;
								leastWeightIndex = i;
							}
						}
						if (!added) {
							// Add a new entry
							if (histogram.size() < hist_cap) {
								// Add a new entry
								HistEntry entry = new HistEntry();
								entry.weight = weight;
								entry.diff = dirlen;
								entry.total = dirlen;
								entry.count = 1;
								histogram.add(entry);
							} else {
								// Try replacing the least weight entry
								if (frac > leastWeight) {
									HistEntry entry = histogram.get(leastWeightIndex);
									entry.weight = weight;
									entry.diff = dirlen;
									entry.total = dirlen;
									entry.count = 1;
								}
							}
						}
					}
				}
			}
		}

		// Now find the highest weight item in the histogram and move the robot's
		// reckoned position by that much
		if (histogram.size() == 0) {
			Log.i("Mapping", "Error: No features found to reckon based on");
			return -1;
		} else {
			int best_index = 0;
			float best_weight = 0;
			for (int i = 0; i < histogram.size(); ++i) {
				//Log.i("Mapping", " Hist ent: [" + histogram.get(i).weight +
				//	"] = " + histogram.get(i).diff);
				if (histogram.get(i).weight >= best_weight) {
					best_weight = histogram.get(i).weight;
					best_index = i;
				}
			}
			float best_diff = histogram.get(best_index).total / histogram.get(best_index).count;
			robotPosition.add(mul(u, best_diff));
			Vec v = new Vec(-u.y, u.x);
			// Assumed perpendicular
			robotPosition.add(mul(v, best_diff*(float)Math.sin(tweakedRot)));
			Log.i("Mapping", "Linearmotion moved " + best_diff);
			return 0;
		}
	}


	// Merge in a line segment adding it to the state
	void mergeSegment(MapSegment seg) {
		// Try to find a colinear line segment to merge with
		boolean merged = false;
		for (MapSegment cand: allSegments) {
			// First, point the two segments in the same direction
			if (dot(cand.vec, seg.vec) < 0.0f) {
				seg.origin.add(seg.vec);
				seg.vec.negate();
			}

			// Discard candidate if they are too dissmilar in direction
			float angleDiff = absangle_between(cand.vec, seg.vec);
			if (angleDiff > 0.5f) {
				// Don't bother doing work in that case
				continue;
			}

			// Project each end of seg onto cand and see if there is
			// an intersection
			Vec u = cand.vec.copy();
			u.normalize();
			float p1 = dot(sub(seg.origin, cand.origin), u);
			float d1 = sub(seg.origin, add(cand.origin, mul(u, p1))).length();
			float p2 = dot(sub(add(seg.origin, seg.vec), cand.origin), u);
			float d2 = sub(add(seg.origin, seg.vec), add(cand.origin, mul(u, p2))).length();

			// Intersection
			float denom = cross(seg.vec, cand.vec);
			float t = cross(sub(cand.origin, seg.origin), cand.vec) / denom;
			float ua = cross(sub(cand.origin, seg.origin), seg.vec) / denom;
			boolean doesIntersect = ((denom != 0) && (ua > 0 && ua < 1) && (t > 0 && t < 1));

			// Relatively close test
			float avglen = 0.5f*(seg.vec.length() + cand.vec.length());
			boolean relativelyClose =
					(d1 < 0.1f*avglen) && (d2 < 0.2f*avglen) ||
							(d1 < 0.2f*avglen) && (d2 < 0.1f*avglen);

			float tlen = cand.vec.length();
			// Is one of the ends of the line seg touching the candidate?
			boolean isTouching =  (d2 < 2*LIDAR_VARIANCE || d1 < 2*LIDAR_VARIANCE);
			// Do the line segments overlap in projection?
			float overlapSlop = 2*LIDAR_VARIANCE;
			boolean projectionDoesOverlap =
					(p2 > -overlapSlop && p2 < tlen+overlapSlop) ||
							(p1 > -overlapSlop && p1 < tlen+overlapSlop);
			//
			if ((projectionDoesOverlap && (isTouching || relativelyClose)) || doesIntersect) {
				// We have an intersection. Add the points from seg to
				// cand and re-regress it.
				cand.pointList.addAll(cand.pointList.size()-1, seg.pointList);

				// Re-regress
				regressSegment(cand);

				// Done
				merged = true;
				break;
			}
		}

		// Couldn't merge? Add as a new global segment
		if (!merged)
			allSegments.add(seg);
	}


	// Merge in a point adding it to the map state
	void mergePoint(MapPoint point) {
		// TODO: Try to merge with edges
		//broadInsertPoint(state, point);
	}

	// Merge new geometry into the map state
	void mergeGeometry(ArrayList<MapPoint> points, ArrayList<MapSegment> segments) {
		// Attatch the segments
		for (MapPoint pt: points)
			mergePoint(pt);

		// Attach the points
		for (MapSegment seg: segments)
			mergeSegment(seg);

		Log.i("Mapping", "Merged, now, Segments: " + segments.size());
	}


	// Process linear motion (post rotation tweaking)
	int angularmotionProcess(ArrayList<MapSegment> segments, float turnHint) {
		// Histogram of differences
		ArrayList<HistEntry> histogram = new ArrayList<HistEntry>();
		int hist_cap = 10;

		// Weight based on the longest segment
		float longestSegLength = 0;
		for (MapSegment to_match: segments) {
			float len = to_match.vec.length();
			longestSegLength = Math.max(longestSegLength, len);
		}

		for (MapSegment to_match: segments) {
			//std::cout << "Rot motion to match...\n";
			// Find the perpendicular distance to the robot
			// by projecting the robot position on to the edge
			float pdist1 = 0;
			float psign1 = 0;
			{
				Vec d = sub(robotPosition, to_match.origin);
				float elen = to_match.vec.length();
				float frac = dot(d, to_match.vec) / (elen * elen);
				psign1 = frac;
				pdist1 = length(d.x - frac*to_match.vec.x, d.y - frac*to_match.vec.y);
			}
			float theta1 = (float)Math.atan2(to_match.vec.y, to_match.vec.x);
			while (theta1 < 0) theta1 += PI;
			//Log.i("Mapping", "Match " + pdist1 + " (theta=" + theta1 + ") with:");

			// Try to match to a segment with a very similar perpendicular distance
			// to the robot
			for (MapSegment a_match: allSegments) {
				float pdist2 = 0;
				float psign2 = 0;
				{
					Vec d = sub(robotPosition, a_match.origin);
					float elen = a_match.vec.length();
					float frac = dot(d, a_match.vec) / (elen * elen);
					psign2 = frac;
					pdist2 = length(d.x - frac*a_match.vec.x, d.y - frac*a_match.vec.y);
				}
				float theta2 = (float)Math.atan2(a_match.vec.y, a_match.vec.x);
				while (theta2 < 0) theta2 += PI;
				if (Math.abs(pdist1 - pdist2) < 2*LIDAR_VARIANCE) {
					// Is a candidate, compute the angluar difference between the two
					float dtheta = theta2 - theta1;
					if (dtheta < 0) dtheta += PI;


					//Log.i("Mapping", " " + pdist2 + " (theta=" + theta2 + ") del = " + dtheta);

					// Now, we have to calculate how reliable the measurement is, off
					// of how oblique the distance to the feature is
					// Use the fraction of the longest to match segment's length
					float len = to_match.vec.length();
					float weight = len / longestSegLength;

					// Note: When frac is small, dirlen will be large, and errors will be
					// scaled up, so use frac as the weight of a measurement
					// Add the dirlen to the histogram
					if (weight > 0.2) {
						// 0.2 -> threshold of useful data
						// below that the wall is almost paralell to our movement
						// and we can't get a distance delta from it
						boolean added = false;
						int leastWeightIndex = 0;
						float leastWeight = 10000;
						for (int i = 0; i < histogram.size(); ++i) {
							HistEntry entry = histogram.get(i);
							if (Math.abs(entry.diff - dtheta) < 2*TURN_TWEAK) {
								entry.weight += weight;
								entry.total += dtheta;
								entry.count += 1;
								added = true;
								break;
							}
							if (entry.weight < leastWeight) {
								leastWeight = entry.weight;
								leastWeightIndex = i;
							}
						}
						if (!added) {
							// Add a new entry
							if (histogram.size() < hist_cap) {
								// Add a new entry
								HistEntry ent = new HistEntry();
								ent.weight = weight;
								ent.diff = dtheta;
								ent.total = dtheta;
								ent.count = 1;
								histogram.add(ent);
							} else {
								// Try replacing the least weight entry
								if (weight > leastWeight) {
									histogram.get(leastWeightIndex).weight = weight;
									histogram.get(leastWeightIndex).diff = dtheta;
									histogram.get(leastWeightIndex).total = dtheta;
									histogram.get(leastWeightIndex).count = 1;
								}
							}
						}
					}
				}
			}
		}
		//

		// Now find the highest weight item in the histogram and move the robot's
		// reckoned position by that much
		if (histogram.size() == 0) {
			Log.i("Mapping", "Error: No features found to reckon based on");
			return -1;
		} else {
			boolean found_within_hint = false;
			int best_index = 0;
			float best_weight = 0;
			for (int i = 0; i < histogram.size(); ++i) {
				//Log.i("Mapping", " Hist ent: [" + histogram.get(i).weight +
				//	"] = " + histogram.get(i).diff);
				if (histogram.get(i).weight >= best_weight) {
					best_weight = histogram.get(i).weight;
					best_index = i;
					found_within_hint = true;
				}
			}
			if (found_within_hint) {
				//Log.i("Mapping", "Best: " + histogram.get(best_index).diff);
				float best_diff = histogram.get(best_index).total / histogram.get(best_index).count;
				//best_diff = -best_diff;
				// Adjust to hint
				if (turnHint < 0) {
					best_diff -= PI;
				}
				robotAngle += best_diff;
				Log.i("Mapping", "Note: Rotation motion moved by " + best_diff);
				return 0;
			} else {
				Log.i("Mapping", "Error: rotationalmotion not found within hint");
				return -1;
			}
		}
	}

	/////////////////////////////////////////////////////

	private ArrayList<MapSegment> allSegments = new ArrayList<MapSegment>();
	private Vec robotPosition = new Vec();
	private float robotAngle = 0;

	///////////////////////////////////////////////////////

	public ArrayList<MapSegment> getSegments() {
		return allSegments;
	}
	public Vec getPosition() {
		return robotPosition;
	}
	public float getAngle() {
		return robotAngle;
	}

	public mapping() {

	}

	public void init() {
		robotAngle = 0;
		robotPosition = new Vec();
		allSegments = new ArrayList<MapSegment>();
	}

	public void initialScan(float angles[], float distances[]) {
		ArrayList<MapPoint> points = new ArrayList<MapPoint>();
		ArrayList<MapSegment> segments = new ArrayList<MapSegment>();
		featureExtract(points, segments, angles, distances);
		mergeGeometry(points, segments);
	}

	public void updateLin(float angles[], float distances[]) {
		// First step is feature extraction, we need to break down the
		// sensor input into points and segments (sequences of 3+ colinear points)
		ArrayList<MapPoint> points = new ArrayList<MapPoint>();
		ArrayList<MapSegment> segments = new ArrayList<MapSegment>();
		featureExtract(points, segments, angles, distances);

		// Now, we approach linear movement in three steps:
		// 1) Do fine adjustment of the rotation of the robot, assuming that
		//    any minor rotations made during linear movement were quite small.
		// 2) Once our rotation has been corrected, match up line segments to
		//    to get a histogram of expected movements, and pick a heavily
		//    median weighted actual movement from those
		// 3) With our updated position, merge the new geometry into the
		//    the global map state
		Vec oldPos = robotPosition.copy();
		float oldTheta = robotAngle;
		float rotChange = rotationTweak(segments);
		updateFeatures(oldPos, oldTheta, points, segments);
		//
		oldPos = robotPosition.copy();
		oldTheta = robotAngle;
		if (linearmotion_process(segments, rotChange) == 0) {
			// If we could process the linear motion, update the map
			updateFeatures(oldPos, oldTheta, points, segments);
			//
			mergeGeometry(points, segments);
		} else {
			Log.i("Mapping", "Failed to update linear");
			deleteFeatures(points, segments);
		}
	}

	public void updateRot(float angles[], float distances[], float turnHint) {
		// First step is feature extraction, we need to break down the
		// sensor input into points and segments (sequences of 3+ colinear points)
		ArrayList<MapPoint> points = new ArrayList<MapPoint>();
		ArrayList<MapSegment> segments = new ArrayList<MapSegment>();
		featureExtract(points, segments, angles, distances);

		// Now, we approach rotational movement in two steps
		// 0) The position is assumed to be unchanged (That is, we can accurately
		//    rotate the robot without issue.
		// 1) We should see fairly similar geometry to what we did before the
		//    rotation, just at different points angularly (modulo the sampling
		//    frequency). That is, we should see line segments with similar
		//    perpendicular distances to the robot, but different angles in the
		//    global space. Try to find the median angle to rotate by from those
		//    anglular differences.
		// 2) With our updated rotation, merge the new geometry into the global
		//    map state.
		Vec oldPos = robotPosition.copy();
		float oldTheta = robotAngle;
		if (angularmotionProcess(segments, turnHint) == 0) {
			// Success, update map
			updateFeatures(oldPos, oldTheta, points, segments);
			//
			mergeGeometry(points, segments);
		} else {
			// Failure, don't know where we are
			deleteFeatures(points, segments);
		}
	}
}
