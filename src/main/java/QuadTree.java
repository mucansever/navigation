import java.util.*;


public class QuadTree {
    public QTreeNode root;
    private String imageRoot;

    public QuadTree(String imageRoot) {
        // Instantiate the root element of the tree with depth 0
        // Use the ROOT_ULLAT, ROOT_ULLON, ROOT_LRLAT, ROOT_LRLON static variables of MapServer class
        root = new QTreeNode("root",MapServer.ROOT_ULLAT,MapServer.ROOT_ULLON,MapServer.ROOT_LRLAT,MapServer.ROOT_LRLON,0);
        // Save the imageRoot value to the instance variable
        this.imageRoot = imageRoot;
        // Call the build method with depth 1
        build(root,1);
    }

    /**
     * Recursively builds quadtree
     * NOTE: Limited with depth 7 with respect to the input
     * @param subTreeRoot: root node
     * @param depth: current depth
     */
    public void build(QTreeNode subTreeRoot, int depth) {
        // Recursive method to build the tree as instructed
        // Don't calculate after depth 7 (Exceeds input)
        if (depth>7)
            return;
        // Calculate lat, lon values required
        double ullat = subTreeRoot.getUpperLeftLatitude();
        double ullon = subTreeRoot.getUpperLeftLongtitude();
        double lrlat = subTreeRoot.getLowerRightLatitude();
        double lrlon = subTreeRoot.getLowerRightLongtitude();
        double midlat = (ullat+lrlat)/2.000;
        double midlon = (ullon+lrlon)/2.000;
        // Prepare name of previous node
        String rawName = subTreeRoot.getName();
        if (rawName.equals("root")) rawName = "";
        // Recursive call to make children for this node
        subTreeRoot.NW = new QTreeNode(rawName+"1",ullat,ullon,midlat,midlon,depth);
        build(subTreeRoot.NW,depth+1);
        subTreeRoot.NE = new QTreeNode(rawName+"2",ullat,midlon,midlat,lrlon,depth);
        build(subTreeRoot.NE,depth+1);
        subTreeRoot.SW = new QTreeNode(rawName+"3",midlat,ullon,lrlat,midlon,depth);
        build(subTreeRoot.SW,depth+1);
        subTreeRoot.SE = new QTreeNode(rawName+"4",midlat,midlon,lrlat,lrlon,depth);
        build(subTreeRoot.SE,depth+1);
    }

    /**
     * Search for given query box in QuadTree
     * @param params: {
     *      "ullat": Upper left latitude of the query box
     *      "ullon": Upper left longitude of the query box
     *      "lrlat": Lower right latitude of the query box
     *      "lrlon": Lower right longitude of the query box
     *      "w": Width of query box in pixels
     * }
     * @return String to Object map with organized array
     */
    public Map<String, Object> search(Map<String, Double> params) {
        // Instantiate a QTreeNode to represent the query box defined by the parameters
        QTreeNode node = new QTreeNode("node",params.get("ullat"),params.get("ullon"),params.get("lrlat"),params.get("lrlon"),0);
        // Calculate the lonDpp value of the query box
        double lonDpp = (params.get("lrlon")-params.get("ullon"))/params.get("w");
        // Call the search() method with the query box and the lonDpp value
        ArrayList<QTreeNode> list = new ArrayList<>();
        search(node,root,lonDpp,list);
        // Call and return the result of the getMap() method to return the acquired nodes in an appropriate way
        return getMap(list);
    }

    /**
     * Formats obtained nodes into a map to form a valid response
     * @param list: list of nodes in query box
     * @return String, Object map with organized array
     */
    private Map<String, Object> getMap(ArrayList<QTreeNode> list) {
        Map<String, Object> map = new HashMap<>();

        // Check if the root intersects with the given query box
        if (list.isEmpty()) {
            map.put("query_success", false);
            return map;
        }

        // Determine upper left and lower right corners
        String[][] list2D = get2D(list);
        QTreeNode ulNode = list.get(0);
        QTreeNode lrNode = list.get(0);
        for (QTreeNode node: list) {
            if (node.getName().equals(list2D[0][0].substring(imageRoot.length(),list2D[0][0].length()-4)))
                ulNode = node;
            if (node.getName().equals(list2D[list2D.length-1][list2D[0].length-1].substring(imageRoot.length(),list2D[0][0].length()-4)))
                lrNode = node;
        }

        // Use the get2D() method to get organized images in a 2D array
        map.put("render_grid", list2D);

        // Upper left latitude of the retrieved grid (Imagine the
        // 2D string array you have constructed as a big picture)
        map.put("raster_ul_lat", ulNode.getUpperLeftLatitude());

        // Upper left longitude of the retrieved grid (Imagine the
        // 2D string array you have constructed as a big picture)
        map.put("raster_ul_lon", ulNode.getUpperLeftLongtitude());

        // Upper lower right latitude of the retrieved grid (Imagine the
        // 2D string array you have constructed as a big picture)
        map.put("raster_lr_lat", lrNode.getLowerRightLatitude());

        // Upper lower right longitude of the retrieved grid (Imagine the
        // 2D string array you have constructed as a big picture)
        map.put("raster_lr_lon", lrNode.getLowerRightLongtitude());

        // Depth of the grid (can be thought as the depth of a single image)
        map.put("depth", ulNode.getDepth());

        map.put("query_success", true);
        return map;
    }

    /**
     * Build 2D array whose images visually correspond to asked query from nodes in the list
     * @param list: list of nodes
     * @return String[][]
     */
    private String[][] get2D(ArrayList<QTreeNode> list) {
        String[][] images;
        TreeMap<Double, TreeSet<String>> sortingMap = new TreeMap<>();
        for (QTreeNode node: list) {
            if (!sortingMap.containsKey(-1*node.getUpperLeftLatitude()))
                sortingMap.put(-1*node.getUpperLeftLatitude(), new TreeSet<>());
            sortingMap.get(-1*node.getUpperLeftLatitude()).add(node.getName());
        }
        int lonSize = sortingMap.firstEntry().getValue().size();
        images = new String[sortingMap.size()][lonSize];
        int i=0;
        for (Double key: sortingMap.keySet()) {
            int j = 0;
            for (String name : sortingMap.get(key)) {
                images[i][j++] = imageRoot+name+".png";
            }
            i++;
        }
        return images;
    }

    public void search(QTreeNode queryBox, QTreeNode tile, double lonDpp, ArrayList<QTreeNode> list) {
        // Retrieve the first depth (zoom level) of the images which intersect with the query
        // box and have a lower lonDPP than the query box.
        if (tile==null || !checkIntersection(queryBox,tile))
            return;
        if (tile.getLonDPP() <= lonDpp)
            list.add(tile);
        else {
            search(queryBox,tile.NW,lonDpp,list);
            search(queryBox,tile.NE,lonDpp,list);
            search(queryBox,tile.SW,lonDpp,list);
            search(queryBox,tile.SE,lonDpp,list);
        }
    }

    /**
     * Check if two nodes intersect
     * @param tile: tile node
     * @param queryBox: query node
     * @return
     */
    public boolean checkIntersection(QTreeNode tile, QTreeNode queryBox) {
        // Return true if two tiles are intersecting with each other
        if (tile.getUpperLeftLatitude()>=queryBox.getUpperLeftLatitude() && tile.getUpperLeftLatitude()>=queryBox.getLowerRightLatitude() &&
                tile.getLowerRightLatitude()>=queryBox.getUpperLeftLatitude() && tile.getLowerRightLatitude()>=queryBox.getLowerRightLatitude())
                return false;
        if (tile.getUpperLeftLatitude()<=queryBox.getUpperLeftLatitude() && tile.getUpperLeftLatitude()<=queryBox.getLowerRightLatitude() &&
                tile.getLowerRightLatitude()<=queryBox.getUpperLeftLatitude() && tile.getLowerRightLatitude()<=queryBox.getLowerRightLatitude())
                return false;
        if (tile.getUpperLeftLongtitude()>=queryBox.getUpperLeftLongtitude() && tile.getUpperLeftLongtitude()>=queryBox.getLowerRightLongtitude() &&
                tile.getLowerRightLongtitude()>=queryBox.getUpperLeftLongtitude() && tile.getLowerRightLongtitude()>=queryBox.getLowerRightLongtitude())
                return false;
        if (tile.getUpperLeftLongtitude()<=queryBox.getUpperLeftLongtitude() && tile.getUpperLeftLongtitude()<=queryBox.getLowerRightLongtitude() &&
                tile.getLowerRightLongtitude()<=queryBox.getUpperLeftLongtitude() && tile.getLowerRightLongtitude()<=queryBox.getLowerRightLongtitude())
                return false;

        return true;
    }
}