package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** The Blob class.
 * @author Qindan Zhu
 */
public class Blob implements Serializable {
    /** Name of file.*/
    private String _name;
    /** Content of file.*/
    private byte[] _content;
    /** Content of file as a string.*/
    private String _contentstr;
    /** Hashcode of the blob.*/
    private String _hashcode;

    /**Constructor for Blob with NAME of the file.
     */
    public Blob(String name) {
        File file = new File(name);
        _name = name;
        _content = Utils.readContents(file);
        _contentstr = Utils.readContentsAsString(file);
        _hashcode = hash();
    }

    /** Return the name of blob.*/
    public String name() {
        return _name;
    }

    /** Return the content of blob.*/
    public byte[] content() {
        return _content;
    }

    /** Return the content of blob as a string.*/
    public String contentstr() {
        return _contentstr;
    }

    /** Return the hashcode.*/
    public String hashcode() {
        return _hashcode;
    }
    /** Use SHA1 to return the hashcode.*/
    private String hash() {
        List<Object> blob = new ArrayList<>();
        blob.add("Blob");
        blob.add(_name);
        blob.add(_content);
        blob.add(_contentstr);
        return Utils.sha1(blob);
    }
}
