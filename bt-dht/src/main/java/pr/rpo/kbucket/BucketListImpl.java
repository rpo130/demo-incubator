package pr.rpo.kbucket;

import pr.rpo.Id;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BucketListImpl implements RoutingTable {
	private final Lock lock = new ReentrantLock();

	private static BucketListImpl bucketList = new BucketListImpl();

	public static RoutingTable table() {
		return bucketList;
	}

	public static RoutingTable getNewOne() {
		return new BucketListImpl();
	}

	/**
	 * maxmum bucket pair
	 */
	static final int K = 8;

	/**
	 * head nodeId
	 */
	public Bucket head;

	private int nodeNum;

	public BucketListImpl() {
		head = new BucketImpl();
	}

	private boolean insert(NodeInfo nodeInfo) {
		if(head.next() == null) {
			head.next(new BucketImpl());
			head.next().pre(head);
		}
		return Bucket.insertBucket(head.next(),nodeInfo);
	}

//	@Override
//	public void updateTimeAndState(NodeInfo nodeInfo) {
//		NodeInfo ni;
//		if((ni= getRef(nodeInfo.getNodeId())) == null) {
//			return;
//		}else {
//			ni.updateTime();
//			ni.setState(NodeInfo.NODE_STATE.GOOD);
//		}
//	}

	@Override
	public NodeInfo[] findNode(NodeId nodeId) {
		return findNode(nodeId,K);
	}

	@Override
	public NodeInfo[] findNode(NodeId nodeId, int k) {
		NodeInfo ni;
		if((ni = getRef(nodeId)) != null) {
			return new NodeInfo[] {ni};
		}else {
			return getK(nodeId, k);
		}
	}

	@Override
	public NodeInfo[] findNodeDeepCopy(NodeId nodeId) {
		NodeInfo ni;
		if((ni = getDeepCoy(nodeId)) != null) {
			return new NodeInfo[] {ni};
		}else {
			NodeInfo[] exist = getK(nodeId, K);
			NodeInfo[] deepCopy = new NodeInfo[exist.length];
			int s=exist.length;
			for(int i=0; i<s; i++) {
				deepCopy[i] = new NodeInfo(exist[i]);
			}

			return deepCopy;
		}
	}

	@Override
	public Bucket findBelongBucket(NodeId nodeId) {
		Bucket b = head.next();
		while(b != null) {
			if(Bucket.isBelong(b, nodeId)) return b;

			b = b.next();
		}
		return null;
	}

	@Override
	public int size() {
		return nodeNum;
	}


	public NodeInfo getRef(NodeId nodeId) {
		Bucket b = head.next();
		while (b != null && !Bucket.isBelong(b, nodeId)) {
			b = b.next();
		}
		if(b == null) {
			return null;
		}else {
			return b.get(nodeId);
		}
	}

	@Override
	public NodeInfo getDeepCoy(NodeId nodeId) {
		NodeInfo ni = getRef(nodeId);
		if(ni != null) {
			return new NodeInfo(ni);
		}else {
			return null;
		}
	}

	@Override
	public boolean tryPutOrUpdate(NodeInfo nodeInfo) {
		lock.lock();
		try {
			NodeInfo oldNodeInfo = getRef(nodeInfo.getNodeId());
			if(oldNodeInfo == null) {
				boolean rFlag = insert(nodeInfo);
				if(rFlag)  {
					this.nodeNum++;
				}

				return rFlag;
			}else {
				oldNodeInfo.setState(nodeInfo.getState());
				oldNodeInfo.setLastContactTime(nodeInfo.getLastContactTime());
				oldNodeInfo.setToken(nodeInfo.getToken());
				oldNodeInfo.setContactInfo(nodeInfo.getContactInfo());
			}
		}finally {
			lock.unlock();
		}
		return true;
	}

	public NodeInfo[] getAll() {
		Bucket b = head.next();
		NodeInfo[] nodeInfos;
		List<NodeInfo> nodeInfosList = new ArrayList<>();

			while (b != null) {
				for (int i=0; i<b.size(); i++) {
					nodeInfosList.add(b.get(i));
				}

				b = b.next();
			}
		if(nodeInfosList.size() != 0) {
			return (NodeInfo[])nodeInfosList.toArray();
		}else {
			return null;
		}
	}

	public NodeInfo[] getK(NodeId nodeId, int num) {
		lock.lock();
		Queue<NodeInfo> knn = null;
		try {
			knn = new LinkedList<>();
			byte[] distanceLeft = null;
			Bucket node = head.next();
			while (node != null) {
				for (NodeInfo ni : node.getAll()) {
					if (distanceLeft == null) {
						distanceLeft = Id.distance(nodeId.getRaw(), ni.getNodeId().getRaw());
					} else {
						if (knn.size() < num) {
							knn.offer(ni);
						} else {
							if (NodeId.compare(new NameSpace(distanceLeft), new NameSpace(Id.distance(nodeId.getRaw(), ni.getNodeId().getRaw()))) > 0) {
								knn.poll();
								knn.offer(ni);
							} else {
								break;
							}
						}
					}
				}
				node = node.next();
			}
		}finally {
			lock.unlock();
		}
		return knn.stream().toArray(NodeInfo[]::new);
	}


	public class BLIterator implements Iterator<NodeInfo> {
		private Bucket bucketRef;
		private int nodeIndex;

		public BLIterator(Bucket b, int nindex) {
			this.bucketRef = b;
			this.nodeIndex = nindex;
		}

		@Override
		public boolean hasNext() {
			if(bucketRef != null) {
				if(nodeIndex < bucketRef.size()) {
					return true;
				}
			}

			return false;
		}

		@Override
		public NodeInfo next() {
			NodeInfo ni = bucketRef.get(nodeIndex);
			this.nodeIndex++;
			if(nodeIndex >= bucketRef.size()) {
				bucketRef = bucketRef.next();
				this.nodeIndex = 0;
			}
			return ni;
		}
	}

	@Override
	public Iterator iterator() {
		return new BLIterator(head.next(),0);
	}

//	private class ThreeBucketWindow {
//		private Bucket win_tail;
//		private Bucket win_mid;
//		private Bucket win_head;
//
//		public ThreeBucketWindow(Bucket assumeBeHead) {
//			win_tail = assumeBeHead.next();
//			win_mid = win_tail != null ? win_tail.next():null;
//			win_head = win_mid != null ? win_mid.next():null;
//		}
//
//		private boolean isFull() {
//			if(win_head == null || win_mid == null || win_tail == null) {
//				return false;
//			}else {
//				return true;
//			}
//		}
//
//		private boolean moveForward() {
//			if(isFull()) {
//				if(win_head.next() != null) {
//					win_head = win_head.next();
//					win_mid = win_head.next();
//					win_tail = win_tail.next();
//					return true;
//				}else {
//					return false;
//				}
//			}else {
//				return false;
//			}
//		}
//
//		private boolean moveBackward() {
//			if(isFull()) {
//				if(win_tail.pre() != head) {
//					win_tail = win_tail.pre();
//					win_mid = win_mid.pre();
//					win_head = win_head.pre();
//					return true;
//				}else {
//					return false;
//				}
//			}else {
//				return false;
//			}
//		}
//
//		public byte[] distanceLow(NodeId nodeId) {
//			return Id.distance(nodeId.getRaw(),win_tail.get(0).getNodeId().getRaw());
//		}
//
//		public byte[] distanceHigh(NodeId nodeId) {
//			return Id.distance(nodeId.getRaw(), win_head.get(win_head.size()-1).getNodeId().getRaw());
//		}
//
//		public NodeInfo[] getCloseK(NodeId nodeId, int num) {
//			if(isFull()) {
//				boolean flag = true;
//				while(flag) {
//					byte[] dl = distanceLow(nodeId);
//					byte[] dh = distanceHigh(nodeId);
//
//					if(moveForward() != true) break;
//
//					if(Bit.compareBigEndian(dl,dh,distanceLow(nodeId),distanceHigh(nodeId)) < 0) {
//						moveBackward();
//						flag = false;
//					}
//				}
//
//				List<Pair<byte[],NodeInfo>> nodeInfos = new ArrayList<>();
//				for (int i=0; i<win_tail.size(); i++) {
//					nodeInfos.add(Pair.from(Id.distance(win_tail.get(i).getNodeId().getRaw(),nodeId.getRaw()),win_tail.get(i)));
//				}
//				if(win_mid != null) {
//					for (int i=0; i<win_mid.size(); i++) {
//						nodeInfos.add(Pair.from(Id.distance(win_mid.get(i).getNodeId().getRaw(),nodeId.getRaw()),win_mid.get(i)));
//					}
//				}
//				if(win_head != null) {
//					for (int i=0; i<win_head.size(); i++) {
//						nodeInfos.add(Pair.from(Id.distance(win_head.get(i).getNodeId().getRaw(),nodeId.getRaw()),win_head.get(i)));
//					}
//				}
//
//				nodeInfos.sort((e1,e2) -> Id.distanceCompare(e1.first(),e2.first()));
//				if(nodeInfos.size() <= num) {
//					return nodeInfos.stream().map(e -> e.senond()).toArray(NodeInfo[]::new);
//				}else {
//					return nodeInfos.stream().skip(nodeInfos.size()-num).map(e -> e.senond()).toArray(NodeInfo[]::new);
//				}
//			}else {
//				List<Pair<byte[],NodeInfo>> nodeInfos = new ArrayList<>();
//				if(win_tail != null) {
//					for (int i = 0; i < win_tail.size(); i++) {
//						nodeInfos.add(Pair.from(Id.distance(win_tail.get(i).getNodeId().getRaw(), nodeId.getRaw()), win_tail.get(i)));
//					}
//				}
//				if(win_mid != null) {
//					for (int i=0; i<win_mid.size(); i++) {
//						nodeInfos.add(Pair.from(Id.distance(win_mid.get(i).getNodeId().getRaw(),nodeId.getRaw()),win_mid.get(i)));
//					}
//				}
//				if(win_head != null) {
//					for (int i=0; i<win_head.size(); i++) {
//						nodeInfos.add(Pair.from(Id.distance(win_head.get(i).getNodeId().getRaw(),nodeId.getRaw()),win_head.get(i)));
//					}
//				}
//
//				nodeInfos.sort((e1,e2) -> Id.distanceCompare(e1.first(),e2.first()));
//
//				if(nodeInfos.size() <= num) {
//					return nodeInfos.stream().map(e -> e.senond()).toArray(NodeInfo[]::new);
//				}else {
//					return nodeInfos.stream().skip(nodeInfos.size()-num).map(e -> e.senond()).toArray(NodeInfo[]::new);
//				}
//			}
//		}
//	}

	public String prettyPrintString() {
		StringBuilder sb = new StringBuilder();
		Bucket b = head.next();
		sb.append("rt-size:").append(size()).append(System.lineSeparator());
		while(b != null) {
			sb.append("bucket:").append(b).append(System.lineSeparator());
			sb.append("size:").append(b.size()).append(System.lineSeparator());
			sb.append("startId:").append(b.startId().hexString()).append(System.lineSeparator());
			sb.append("bitMask:").append(b.idMask().hexString()).append(System.lineSeparator());
			for(NodeInfo ni : b.getAll()) {
				sb.append(ni.toString()).append(System.lineSeparator());
			}
			sb.append(System.lineSeparator());
			b = b.next();
		}
		return sb.toString();
	}
}
