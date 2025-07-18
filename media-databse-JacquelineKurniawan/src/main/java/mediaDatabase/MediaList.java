package mediaDatabase;

import javax.xml.transform.Result;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

/**
 * Author: Jacqueline Kurniawan
 */
public class MediaList {
    public ArrayList<Media> sortTitle(ArrayList<Media> mdList){
        for(int i = 0; i < mdList.size()-1; i++){
            boolean flag = true;
            for(int j = i+1; j < mdList.size(); j++){
                if(mdList.get(i).getTitle().compareToIgnoreCase(mdList.get(j).getTitle()) > 0){
                    flag = false;
                    Media temp = mdList.get(i);
                    mdList.set(i, mdList.get(j));
                    mdList.set(j, temp);
                }
            }
            if(flag){
                i = mdList.size();
            }
        }
        return mdList;
    }

    public ArrayList<Media> sortCreator(ArrayList<Media> mdList){
        for(int i = 0; i < mdList.size()-1; i++){
            boolean flag = true;
            for(int j = i+1; j < mdList.size(); j++){
                if(mdList.get(i).getCreator().compareToIgnoreCase(mdList.get(j).getCreator()) > 0){
                    flag = false;
                    Media temp = mdList.get(i);
                    mdList.set(i, mdList.get(j));
                    mdList.set(j, temp);
                }
            }
            if(flag){
                i = mdList.size();
            }
        }
        return mdList;
    }


}
