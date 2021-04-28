package christiansplayer;

import java.util.Stack;
import java.util.HashMap;
import java.util.Random;
import battlecode.common.*;

public strictfp class RobotPlayer {
	static RobotController rc;

	static Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
			Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST };

	static HashMap<MapLocation, MapLocation> visited = new HashMap<MapLocation, MapLocation>();
	static Random oracle = new Random();

	static MapLocation hqLocation;
	static boolean designSchoolBuilt = false;
	static boolean fulfillmentCenterBuilt = false;

	static RobotType[] spawnedByMiner = { RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
			RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN };
	static int minersBuilt = 0;
	static int gameStage = 0;
	static int turnCount;
	static int soupRequirement = 75;
	static int blockChainBid = 25;
	static int code = 42069;
	static int[] currentKnowledge = { 0, 0, 0, 0, 0, 0, 42069 };
	// static int[] currentKnowledge = new int[]{0,0,0,0,0,0,42069}; //used to keep
	// track of how many of each building/thing we have, 42069 used to distinguish
	// our messages from the other team in the blockchain

	/*
	 * Block-Chain Message Indexes of current knowledge - ID: 222 currentKnowledge
	 * [0] - # of net guns currentKnowledge [1] - # of refineries currentKnowledge
	 * [2] - # of vaporators currentKnowledge [3] - # of design schools
	 * currentKnowledge [4] - # of fulfilment centers currentKnowledge [5] - # of
	 * currentKnowledge [6] - code
	 */

	/**
	 * run() is the method that is called when a robot is instantiated in the
	 * Battlecode world. If this method returns, the robot dies!
	 **/
	@SuppressWarnings("unused")
	public static void run(RobotController rc) throws GameActionException {

		// This is the RobotController object. You use it to perform actions from this
		// robot,
		// and to get information on its current status.
		RobotPlayer.rc = rc;

		turnCount = 0;
		gameStage = 0;

		System.out.println("I'm a " + rc.getType() + " and I just got created!");
		while (true) {
			turnCount += 1;
			// Try/catch blocks stop unhandled exceptions, which cause your robot to explode
			try {
				// Here, we've separated the controls into a different method for each
				// RobotType.
				// You can add the missing ones or rewrite this into your own control structure.
				// System.out.println("I'm a " + rc.getType() + "! Location " +
				// rc.getLocation());
				switch (rc.getType()) {
				case HQ:
					runHQ();
					break;
				case MINER:
					runMiner();
					break;
				case REFINERY:
					runRefinery();
					break;
				case VAPORATOR:
					runVaporator();
					break;
				case DESIGN_SCHOOL:
					runDesignSchool();
					break;
				case FULFILLMENT_CENTER:
					runFulfillmentCenter();
					break;
				case LANDSCAPER:
					runLandscaper();
					break;
				case DELIVERY_DRONE:
					runDeliveryDrone();
					break;
				case NET_GUN:
					runNetGun();
					break;
				}

				// Clock.yield() makes the robot wait until the next turn, then it will perform
				// this loop again
				Clock.yield();

			} catch (Exception e) {
				System.out.println(rc.getType() + " Exception");
				e.printStackTrace();
			}
		}
	}

	static void runHQ() throws GameActionException {
		// tryBlockchain(gameStage);
		// Transaction[] compareBlock = rc.getBlock(gameStage); //If I uncomment this
		// the code does basically nothing

		// Blockchain Idea:
		// At each stage check the block chain for messages with our code "42069" at the
		// end (secret code is subject to change)
		// If we have a match we then compare our currentKnowledge with the new block.
		// If there are any differences we update our currentKnowledge, and continue on
		// normally.
		// This way all robots should always be on the same page

		// Problems:
		// It seems as though whenever we s.getBlock the program freezes.

		// System.out.println(" Compare Block: " + compareBlock);

		// Number of miners will be determined by amount of soup that is close by.
		MapLocation[] soups = rc.senseNearbySoup();

		int ratio_of_miners_to_sensed_soup = (int) (soups.length * 1.5);

		for (Direction dir : directions) {
			if (minersBuilt < ratio_of_miners_to_sensed_soup && tryBuild(RobotType.MINER, dir)) {
				minersBuilt++;
				// System.out.println("Current Block: " + rc.getBlock());
			}
		}
	}

	/*
	 * Block-Chain Message Indexes of current knowledge - ID: 222 currentKnowledge
	 * [0] - # of net guns currentKnowledge [1] - # of refineries currentKnowledge
	 * [2] - # of vaporators currentKnowledge [3] - # of design schools
	 * currentKnowledge [4] - # of fulfilment centers currentKnowledge [5] - # of
	 * currentKnowledge [6] - code
	 */
	static void addToKnowledgeBase(RobotType robot) {
		if (RobotType.NET_GUN == robot) {
			currentKnowledge[0]++;
		}

		if (RobotType.REFINERY == robot) {
			currentKnowledge[1]++;
		}

		if (RobotType.VAPORATOR == robot) {
			currentKnowledge[2]++;
		}

		if (RobotType.DESIGN_SCHOOL == robot) {
			currentKnowledge[3]++;
		}

		if (RobotType.FULFILLMENT_CENTER == robot) {
			currentKnowledge[4]++;
		}
	}

	static boolean purchaseBuilding(RobotType robot, int cost) throws GameActionException {
		if (rc.getTeamSoup() >= (cost + blockChainBid)) {
			for (Direction dir : directions) {
				if (tryBuild(robot, dir)) {
					addToKnowledgeBase(robot);
					if (rc.canSubmitTransaction(currentKnowledge, blockChainBid)) {
						rc.submitTransaction(currentKnowledge, blockChainBid);
						System.out.println("I submitted a transaction!: " + currentKnowledge);
					}
					return true;
				}
			}
		}
		return false;
	}

	static void findHQ() {
		RobotInfo[] info = rc.senseNearbyRobots();
		for (RobotInfo robot : info) {
			if (robot.type == RobotType.HQ && robot.team == rc.getTeam()) {
				hqLocation = robot.getLocation();
			}
		}
	}

	static boolean mineAllDirections() throws GameActionException {
		// if we are carrying 75 soup or more try refining
		boolean didRefine = false;
		if (rc.getSoupCarrying() < (int) (RobotType.MINER.soupLimit * (3.0 / 4.0))) {
			for (Direction dir : directions) {
				didRefine = didRefine || tryMine(dir);
			}
		}
		return didRefine;
	}

	static void refineAllDirections() throws GameActionException {
		if (rc.getLocation().distanceSquaredTo(hqLocation) <= 2) {
			for (Direction dir : directions) {
				tryRefine(dir);
			}
		}
	}

	// Finds best move that goes towards the or closest refinery
	static void moveTowardsHQ() throws GameActionException {
		Direction toHQ = rc.getLocation().directionTo(hqLocation);
		if (tryMove(toHQ)) {
			System.out.println("I moved towards the HQ");
		} else {
			tryMove(toHQ.rotateRight());
		}
	}

	static void moveTowardsSoup() throws GameActionException {
		int closestSoupDistance = Integer.MAX_VALUE;
		MapLocation closestSoupLocation = null;
		MapLocation[] soups = rc.senseNearbySoup();

		// When we find soup around try to move towards it
		if (soups.length > 0) {
			// Finds the closest soup out of all the soups we sense
			for (MapLocation soup : soups) {
				int distanceToSoup = rc.getLocation().distanceSquaredTo(soup);
				if (distanceToSoup < closestSoupDistance) {
					closestSoupDistance = distanceToSoup;
					closestSoupLocation = soup;
				}
			}

			Direction toSoup = rc.getLocation().directionTo(closestSoupLocation);

			// Move in fastest direction to the closest soup
			/*
			 * boolean didMove = tryMove(toSoup); for(int i = 0; i < 8; i++) { if(didMove ==
			 * false) { toSoup.rotateLeft(); didMove = tryMove(toSoup); } }
			 */
			// if(tryMove(toSoup)) {
			// System.out.println("I moved towards the soup");
			// } else {
			// tryMove(toSoup.rotateLeft());
			// }
			if (rc.canMove(toSoup)) {
				rc.move(toSoup);
			} else {
				tryMove(randomDirection());
			}
			// When there is no soup around to get
		} // else {
			// moveTowardsHQ();
			// }
	}

	static void buildVaporator() throws GameActionException {
		RobotInfo[] robots = rc.senseNearbyRobots();
		int number_of_vapes = 0;

		for (RobotInfo rob : robots) {
			if (rob.type == RobotType.VAPORATOR) {
				number_of_vapes++;
			}
		}

		// Builds a vape if sense less than 7 around.
		if (number_of_vapes <= 7 && rc.getTeamSoup() >= RobotType.VAPORATOR.cost) {
			for (Direction dir : directions) {
				if (!rc.adjacentLocation(dir).isAdjacentTo(hqLocation)) {
					boolean didBuild = tryBuild(RobotType.VAPORATOR, dir);
					if (didBuild) {
						break;
					}
				}
			}
		}
	}

	static void buildDesignSchool() throws GameActionException {
		RobotInfo[] robots = rc.senseNearbyRobots();

		for (RobotInfo bot : robots) {
			if (bot.type == RobotType.DESIGN_SCHOOL) {
				designSchoolBuilt = true;
				// End building design school method early
				return;
			}
		}

		// Builds design school wherever it can
		for (Direction dir : directions) {
			if (!rc.adjacentLocation(dir).isAdjacentTo(hqLocation)) {
				boolean didBuild = tryBuild(RobotType.DESIGN_SCHOOL, dir);
				if (didBuild) {
					break;
				}
			}
		}
	}

	static void buildFulfillmentCenter() throws GameActionException {
		RobotInfo[] robots = rc.senseNearbyRobots();

		for (RobotInfo bot : robots) {
			if (bot.type == RobotType.FULFILLMENT_CENTER) {
				designSchoolBuilt = true;
				// End building design school method early
				return;
			}
		}

		// Builds design school wherever it can
		for (Direction dir : directions) {
			if (!rc.adjacentLocation(dir).isAdjacentTo(hqLocation)) {
				boolean didBuild = tryBuild(RobotType.FULFILLMENT_CENTER, dir);
				if (didBuild) {
					break;
				}
			}
		}
	}

	static void runMiner() throws GameActionException {

		// find HQ in beginning
		if (hqLocation == null) {
			findHQ();
		}

		// Need only one design school, done by whatever bot first tells.
		if (!designSchoolBuilt) {
			buildDesignSchool();
		}

		// Try to build vaporators first
		// These should pretty consistently be build
		// See build method to see condition upon which we have too many vaporators.
		buildVaporator();

		// Only really need one of these every so often.
		if (!fulfillmentCenterBuilt) {
			buildFulfillmentCenter();
		}

		// Runs back towards HQ when amount of soup is large.
		if (rc.getSoupCarrying() >= (int) (RobotType.MINER.soupLimit * (3.0 / 4.0))) {
			refineAllDirections();
			moveTowardsHQ();
		} else {
			// Otherwise look for more soup
			boolean didRefine = mineAllDirections();
			// Look for more soup when unable to mine in squares around
			if (!didRefine) {
				moveTowardsSoup();
			}
		}
	}

	static void runRefinery() throws GameActionException {
		// System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
	}

	static void runVaporator() throws GameActionException {

		System.out.println("OH MY GOSH IM A VAPORATOR;");
	}

	static void runDesignSchool() throws GameActionException {

	}

	static void runFulfillmentCenter() throws GameActionException {
		for (Direction dir : directions)
			tryBuild(RobotType.DELIVERY_DRONE, dir);
	}

	static void runLandscaper() throws GameActionException {

	}

	static void runDeliveryDrone() throws GameActionException {
		Team enemy = rc.getTeam().opponent();
		if (!rc.isCurrentlyHoldingUnit()) {
			// See if there are any enemy robots within capturing range
			RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);

			if (robots.length > 0) {
				// Pick up a first robot within range
				rc.pickUpUnit(robots[0].getID());
				System.out.println("I picked up " + robots[0].getID() + "!");
			}
		} else {
			// No close robots, so search for robots within sight radius
			tryMove(randomDirection());
		}
	}

	static void runNetGun() throws GameActionException {

	}

	/**
	 * Returns a random Direction.
	 *
	 * @return a random Direction
	 */
	static Direction randomDirection() {
		return directions[(int) (Math.random() * directions.length)];
	}

	/**
	 * Returns a random RobotType spawned by miners.
	 *
	 * @return a random RobotType
	 */
	static RobotType randomSpawnedByMiner() {
		return spawnedByMiner[(int) (Math.random() * spawnedByMiner.length)];
	}

	static boolean tryMove() throws GameActionException {
		for (Direction dir : directions)
			if (tryMove(dir))
				return true;
		return false;
	}

	/**
	 * Attempts to move in a given direction.
	 *
	 * @param dir The intended direction of movement
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	static boolean tryMove(Direction dir) throws GameActionException {
		if (rc.isReady() && rc.canMove(dir)) {
			rc.move(dir);
			return true;
		} else
			return false;
	}

	/**
	 * Attempts to build a given robot in a given direction.
	 *
	 * @param type The type of the robot to build
	 * @param dir  The intended direction of movement
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
		if (rc.isReady() && rc.canBuildRobot(type, dir)) {
			rc.buildRobot(type, dir);
			return true;
		} else
			return false;
	}

	/**
	 * Attempts to mine soup in a given direction.
	 *
	 * @param dir The intended direction of mining
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	static boolean tryMine(Direction dir) throws GameActionException {
		if (rc.isReady() && rc.canMineSoup(dir)) {
			rc.mineSoup(dir);
			return true;
		} else
			return false;
	}

	/**
	 * Attempts to refine soup in a given direction.
	 *
	 * @param dir The intended direction of refining
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	static boolean tryRefine(Direction dir) throws GameActionException {
		if (rc.isReady() && rc.canDepositSoup(dir)) {
			rc.depositSoup(dir, rc.getSoupCarrying());
			return true;
		} else
			return false;
	}

	static void tryBlockchain(int stage) throws GameActionException {
		int[] message = new int[7];

		for (int i = 0; i < 7; i++) {
			message[i] = 123;
		}
		if (rc.canSubmitTransaction(message, 10))
			rc.submitTransaction(message, 10);
		// System.out.println(rc.getRoundMessages(turnCount-1));
	}
}