package gitlet;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**The commit class.
 * @author Qindan Zhu
 */
public class Commit implements Serializable {
    /** Message of commit.*/
    private String _message;
    /** Include blobs of the commit.*/
    private HashMap<String, Blob> _blobs = new HashMap<>();
    /** Blob names of the commit.*/
    private ArrayList<String> _blobnames = new ArrayList<>();
    /** Blob contents of the commits in the format of string.*/
    private ArrayList<String> _blobcontentstrs = new ArrayList<>();
    /** Time of the commit.*/
    private String _time;
    /** Parent id of the commit.*/
    private String _parentid;
    /** Hashcode of the commit.*/
    private String _hashcode;
    /** Marker for merge commit.*/
    private boolean _merge;
    /** Merge commit id.*/
    private String _mergeid;

    /** Return the message of the commit.*/
    public String message() {
        return _message;
    }

    /** Return the time of the commit.*/
    public String time() {
        return _time;
    }

    /** return the blobs of the commit.*/
    public HashMap<String, Blob> blobs() {
        return _blobs;
    }

    /** return the blob names of the commit.*/
    public ArrayList<String> blobnames() {
        return _blobnames;
    }

    /** Return the blob contents of the commit.*/
    public ArrayList<String> blobcontentstrs() {
        return _blobcontentstrs;
    }

    /** Return the parentid of the commit.*/
    public String parentid() {
        return _parentid;
    }

    /** Return the hashcode of the commit.*/
    public String hashcode() {
        return _hashcode;
    }

    /**Return the marker of a merge commit.*/
    public Boolean merge() {
        return _merge;
    }

    /** Return the merge id.*/
    public String mergeid() {
        return _mergeid;
    }
    /**Constructor for normal commit based on MESSAGE,
     * BLOBS, PARENTID.*/
    public Commit(String message, HashMap<String, Blob> blobs,
                  String parentid) {
        _message = message;
        _blobs = blobs;
        _parentid = parentid;
        ZonedDateTime time = ZonedDateTime.now();
        _time = time.format(DateTimeFormatter.ofPattern
                ("EEE MMM d HH:mm:ss yyyy xxxx"));
        for (String blobname:blobs.keySet()) {
            _blobnames.add(blobname);
            _blobcontentstrs.add(blobs.get(blobname).contentstr());
        }
        _hashcode = hash();
    }

    /**Constructor for a merge commit based on MESSAGE,
     * BLOBS, PARENTID and MERGEPARENTID.*/
    public Commit(String message, HashMap<String, Blob> blobs,
                  String parentid, String mergeparentid) {
        _mergeid = mergeparentid;
        _merge = true;
        _message = message;
        _blobs = blobs;
        _parentid = parentid;
        ZonedDateTime time = ZonedDateTime.now();
        _time = time.format(DateTimeFormatter.ofPattern
                ("EEE MMM d HH:mm:ss yyyy xxxx"));
        for (String blobname:blobs.keySet()) {
            _blobnames.add(blobname);
            _blobcontentstrs.add(blobs.get(blobname).contentstr());
        }
        _hashcode = hash();
    }


    /**Construct for initial commit given the MESSAGE and BRANCH.*/
    public Commit(String message) {
        _message = message;
        _time = "Wed Dec 31 16:00:00 1969 -0800";
        _parentid = "";
        _hashcode = hash();
    }
    /** Use SHA1 to return the hashcode for commit.*/
    private String hash() {
        List<Object> commit = new ArrayList<>();
        commit.add("Commit");
        commit.add(_message);
        commit.add(_parentid);
        commit.add(_time);
        commit.addAll(_blobnames);
        commit.addAll(_blobcontentstrs);
        if (_merge) {
            commit.add(_mergeid);
        }
        return Utils.sha1(commit);
    }

}
