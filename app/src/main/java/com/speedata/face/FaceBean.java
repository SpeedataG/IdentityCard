package com.speedata.face;

import java.util.List;

/**
 * Created by brxu on 2017/1/6.
 */

public class FaceBean {


    /**
     * faces1 : [{"face_rectangle":{"width":1138,"top":154,"left":210,"height":1138},
     * "face_token":"42e7b9e4f390ec40f9a32da532cae9b6"}]
     * faces2 : [{"face_rectangle":{"width":56,"top":43,"left":24,"height":56},
     * "face_token":"e9bf2f305dfb8157ac1f3e5857d79b55"}]
     * time_used : 1439
     * thresholds : {"1e-3":65.3,"1e-5":76.5,"1e-4":71.8}
     * confidence : 81.966
     * image_id2 : fQWZfKXqBLxjfzQCyaww7g==
     * image_id1 : WyipeFwSu4gD4nDJiF+zQg==
     * request_id : 1483689382,b71e716e-88fa-45d0-a84d-89f69aed93b8
     */

    public int time_used;
    /**
     * 1e-3 : 65.3
     * 1e-5 : 76.5
     * 1e-4 : 71.8
     */

//    public ThresholdsBean thresholds;
    /**
     * faces1 : [{"face_rectangle":{"width":1138,"top":154,"left":210,"height":1138},
     * "face_token":"42e7b9e4f390ec40f9a32da532cae9b6"}]
     * faces2 : [{"face_rectangle":{"width":56,"top":43,"left":24,"height":56},
     * "face_token":"e9bf2f305dfb8157ac1f3e5857d79b55"}]
     * confidence : 81.966
     * image_id2 : fQWZfKXqBLxjfzQCyaww7g==
     * image_id1 : WyipeFwSu4gD4nDJiF+zQg==
     * request_id : 1483689382,b71e716e-88fa-45d0-a84d-89f69aed93b8
     */

    public double confidence;
    public String image_id2;
    public String image_id1;
    public String request_id;
    /**
     * face_rectangle : {"width":1138,"top":154,"left":210,"height":1138}
     * face_token : 42e7b9e4f390ec40f9a32da532cae9b6
     */

    public List<Faces1Bean> faces1;
    /**
     * face_rectangle : {"width":56,"top":43,"left":24,"height":56}
     * face_token : e9bf2f305dfb8157ac1f3e5857d79b55
     */

    public List<Faces2Bean> faces2;

    public static class Faces1Bean {
        /**
         * width : 1138
         * top : 154
         * left : 210
         * height : 1138
         */

        public FaceRectangleBean face_rectangle;
        public String face_token;

        public static class FaceRectangleBean {
            public int width;
            public int top;
            public int left;
            public int height;
        }
    }

    public static class Faces2Bean {
        /**
         * width : 56
         * top : 43
         * left : 24
         * height : 56
         */

        public FaceRectangleBean face_rectangle;
        public String face_token;

        public static class FaceRectangleBean {
            public int width;
            public int top;
            public int left;
            public int height;
        }
    }
}
