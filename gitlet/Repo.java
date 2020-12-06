package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;

/** The repo class.
 * @author Qindan Zhu
 */
public class Repo implements Serializable {
    /** The maximum.*/
    static final int MAXVALUE = 1345555;
    /** The stage folder.*/
    static final File STAGE_FOLDER = new File(Main.MAIN_FOLDER, "staging");
    /** The branch folder.*/
    static final File BRANCH_FOLDER = new File(Main.MAIN_FOLDER, "ref");
    /** The commit folder.*/
    static final File COMMIT_FOLDER = new File(Main.MAIN_FOLDER, "commit");

    /** Marker for a initialized repo.*/
    private Boolean _initialized;
    /** The removed filenames.*/
    private ArrayList<String> _remove = new ArrayList<>();
    /** The filenames of those in the current stage.*/
    private ArrayList<String> _stage = new ArrayList<>();
    /** The currently untracked files.*/
    private ArrayList<String> _untracked = new ArrayList<>();
    /** The commit hashcodes array.*/
    private ArrayList<String> _commits = new ArrayList<>();
    /** The branch array.*/
    private ArrayList<String> _branches = new ArrayList<>();
    /** Splitid for each branch.*/
    private HashMap<String, String> _splitid = new HashMap<>();
    /** The current blobs.*/
    private HashMap<String, Blob> _currblobs = new HashMap<>();
    /** The hashcode for current commit.*/
    private String _currcommit;
    /** The current branch name.*/
    private String _currbranch;


    /**Constructor for repo.*/
    public Repo() {
        _initialized = false;
    }

    /**Return the repo from record file.*/
    public static Repo fromFile() {
        return Utils.readObject(Main.RECORD, Repo.class);
    }

    /** Main processor to handle different commands given the ARGS.*/
    public void process(String[] args) throws IOException {
        if (!args[0].equals("init") && !_initialized) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        switch (args[0]) {
        case "init":
            init(args);
            break;
        case "add":
            add(args);
            break;
        case "commit":
            commit(args);
            break;
        case "rm":
            rm(args);
            break;
        case "log":
            log(args);
            break;
        case "global-log":
            globallog(args);
            break;
        case "find":
            find(args);
            break;
        case "status":
            status(args);
            break;
        case "checkout":
            checkout(args);
            break;
        case "branch":
            branch(args);
            break;
        case "rm-branch":
            rmbranch(args);
            break;
        case "reset":
            reset(args);
            break;
        case "merge":
            merge(args);
            break;
        default:
            break;
        }
    }

    /**command init given ARGS.*/
    private void init(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        } else {
            if (!_initialized) {
                Main.MAIN_FOLDER.mkdir();
                STAGE_FOLDER.mkdir();
                BRANCH_FOLDER.mkdir();
                COMMIT_FOLDER.mkdir();
                _currbranch = "master";
                Commit newcommit = new Commit("initial commit");
                _commits.add(newcommit.hashcode());
                _branches.add(_currbranch);
                _currcommit = newcommit.hashcode();
                _initialized = true;
                Utils.writeObject(Utils.join(COMMIT_FOLDER,
                        newcommit.hashcode()),
                        newcommit);
                Utils.writeContents(Utils.join(BRANCH_FOLDER, _currbranch),
                        _currcommit);
            } else {
                System.out.println("A Gitlet version-control system "
                        + "already exists in the current directory.");
                System.exit(0);
            }
        }
    }

    /**Command add given the ARGS.*/
    private void add(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        } else {
            if (!_initialized) {
                System.out.println("Not in an initialized Gitlet directory.");
                System.exit(0);
            } else {
                File file = Utils.join(Main.CURR_FOLDER, args[1]);
                if (!file.exists()) {
                    System.out.println("File does not exist.");
                    System.exit(0);
                } else {
                    _remove.remove(args[1]);
                    _untracked.remove(args[1]);
                    Blob newblob = new Blob(args[1]);
                    _currblobs.put(newblob.name(), newblob);
                    _stage.add(args[1]);
                    Commit savedcommit = Utils.readObject(Utils.join(
                            COMMIT_FOLDER, _currcommit), Commit.class);
                    if (savedcommit.blobs() != null
                            && savedcommit.blobs().containsKey(args[1])) {
                        String savedhash;
                        savedhash = savedcommit.blobs().get(
                                args[1]).hashcode();
                        if (savedhash.equals(newblob.hashcode())) {
                            _currblobs.remove(newblob.name());
                            _stage.remove(newblob.name());
                            Utils.join(STAGE_FOLDER,
                                    newblob.hashcode()).delete();
                        } else {
                            Utils.join(STAGE_FOLDER, savedhash).delete();
                            Utils.writeObject(Utils.join(STAGE_FOLDER,
                                    newblob.hashcode()), newblob);
                        }
                    } else {
                        Utils.writeObject(Utils.join(STAGE_FOLDER,
                                newblob.hashcode()), newblob);
                    }
                }
            }
        }
    }

    /**Command commit given the ARGS.*/
    private void commit(String[] args) {
        if (args.length > 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        } else {
            if (args.length == 1 || args[1].equals("")) {
                System.out.println("Please enter a commit message.");
                System.exit(0);
            } else {
                if (_stage.isEmpty()
                        && _remove.isEmpty()) {
                    System.out.println("No changes added to the commit.");
                    System.exit(0);
                } else {
                    for (String rmfile : _remove) {
                        if (_currblobs.containsKey(rmfile)) {
                            _currblobs.remove(rmfile);
                        }
                    }
                    _remove.clear();
                    Commit newcommit;
                    if (_merge) {
                        newcommit = new Commit(args[1], _currblobs,
                                _currcommit, _mergeid);
                    } else {
                        newcommit = new Commit(args[1], _currblobs,
                                _currcommit);
                    }
                    _commits.add(newcommit.hashcode());
                    _currcommit = newcommit.hashcode();
                    for (String filename : _stage) {
                        Utils.join(STAGE_FOLDER, filename).delete();
                    }
                    Utils.writeObject(Utils.join(COMMIT_FOLDER,
                            newcommit.hashcode()), newcommit);
                    Utils.writeContents(Utils.join(BRANCH_FOLDER, _currbranch),
                            _currcommit);
                    _stage.clear();
                    _untracked.clear();
                }
            }
        }
    }

    /**Command log given ARGS.*/
    private void log(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        } else {
            String headcommitstr = Utils.readContentsAsString(
                    Utils.join(BRANCH_FOLDER, _currbranch));
            Commit thiscommit = Utils.readObject(Utils.join(COMMIT_FOLDER,
                    headcommitstr), Commit.class);
            while (thiscommit != null && !thiscommit.parentid().equals("")) {
                System.out.println("===");
                System.out.println("commit " + thiscommit.hashcode());
                if (thiscommit.merge()) {
                    System.out.println("Merge: "
                            + _currcommit.substring(0, 7)
                            + " " + thiscommit.mergeid().substring(0, 7));
                }
                System.out.println("Date: " + thiscommit.time());
                System.out.println(thiscommit.message());
                System.out.println();
                thiscommit = Utils.readObject(Utils.join(COMMIT_FOLDER,
                        thiscommit.parentid()), Commit.class);
            }
            System.out.println("===");
            System.out.println("commit " + thiscommit.hashcode());
            System.out.println("Date: " + thiscommit.time());
            System.out.println(thiscommit.message());
        }
    }

    /**Command global-log given ARGS.*/
    private void globallog(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        } else {
            for (String commithash:_commits) {
                Commit thiscommit = Utils.readObject(Utils.join(COMMIT_FOLDER,
                        commithash), Commit.class);
                System.out.println("===");
                System.out.println("commit " + thiscommit.hashcode());
                System.out.println("Date: " + thiscommit.time());
                System.out.println(thiscommit.message());
                System.out.println();
            }
        }
    }

    /**Command find given ARGS.*/
    private void find(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        } else {
            boolean mark = false;
            for (String commithash:_commits) {
                Commit thiscommit = Utils.readObject(Utils.join(COMMIT_FOLDER,
                        commithash), Commit.class);
                if (thiscommit.message().contains(args[1])) {
                    System.out.println(thiscommit.hashcode());
                    mark = true;
                }
            }
            if (!mark) {
                System.out.println("Found no commit with that message.");
            }
        }
    }

    /**Command status given ARGS.*/
    private void status(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        } else {
            System.out.println("=== Branches ===");
            Collections.sort(_branches);
            for (String name : _branches) {
                if (name.equals(_currbranch)) {
                    System.out.print("*");
                }
                System.out.println(name);
            }
            System.out.println();
            System.out.println("=== Staged Files ===");
            Collections.sort(_stage);
            for (String name: _stage) {
                System.out.println(name);
            }

            System.out.println();
            System.out.println("=== Removed Files ===");
            Collections.sort(_remove);
            for (String name: _remove) {
                System.out.println(name);
            }

            System.out.println();
            System.out.println("=== Modifications Not "
                    + "Staged For Commit ===");
            System.out.println();
            System.out.println("=== Untracked Files ===");
            Collections.sort(_untracked);
            for (String name: _untracked) {
                System.out.println(name);
            }
        }
    }

    /**Command rm given ARGS.*/
    private void rm(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        } else {
            boolean mark = false;
            if (!_stage.isEmpty() && _stage.contains(args[1])) {
                Utils.join(STAGE_FOLDER, _currblobs.
                        get(args[1]).hashcode()).delete();
                _currblobs.remove(args[1]);
                _stage.remove(args[1]);
                _untracked.add(args[1]);
                mark = true;
            }
            Commit thiscommit = Utils.readObject(Utils.join(COMMIT_FOLDER,
                    _currcommit), Commit.class);
            if (thiscommit.blobnames().contains(args[1])) {
                _remove.add(args[1]);
                _untracked.remove(args[1]);
                Utils.join(Main.CURR_FOLDER, args[1]).delete();
                mark = true;
            }

            if (!mark) {
                System.out.println("No reason to remove the file.");
                System.exit(0);
            }

        }
    }

    /**Command checkout given the ARGS from input.*/
    private void checkout(String[] args) {
        if (args.length == 3) {
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            } else {
                checkouthelper(_currcommit, args[2]);
            }
        } else {
            if (args.length == 4) {
                if (!args[2].equals("--")) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                for (String commitid: _commits) {
                    if (commitid.substring(0,
                            args[1].length()).equals(args[1])) {
                        args[1] = commitid;
                    }
                }
                if (!_commits.contains(args[1])) {
                    System.out.println("No commit with that id exists.");
                } else {
                    checkouthelper(args[1], args[3]);
                }
            } else {
                if (args.length == 2) {
                    checkoutbranch(args);
                } else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
            }
        }
    }

    /** the helper function for checkout given the COMMITID to load the file
     * and the changed FILENAME.
     */
    private void checkouthelper(String commitid, String filename) {
        Commit commit = Utils.readObject(Utils.join(COMMIT_FOLDER,
                commitid), Commit.class);
        if (!commit.blobs().containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        } else {
            Utils.writeContents(Utils.join(Main.CURR_FOLDER, filename),
                    (Object) commit.blobs().get(filename).content());
        }
    }

    /**Helper function for checkout branch given ARGS.*/
    private void checkoutbranch(String[] args) {
        if (!_branches.contains(args[1])) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (_currbranch.equals(args[1])) {
            System.out.println("No need to checkout"
                    + " the current branch.");
            System.exit(0);
        }


        String head = Utils.readContentsAsString(Utils.join(BRANCH_FOLDER,
                        args[1]));
        Commit headcommit = Utils.readObject(Utils.join(COMMIT_FOLDER,
                head), Commit.class);
        untrackcheck(headcommit);
        for (String filename : _currblobs.keySet()) {
            Utils.join(Main.CURR_FOLDER, filename).delete();
        }
        _currblobs = headcommit.blobs();
        _currcommit = headcommit.hashcode();
        for (String filename : _currblobs.keySet()) {
            Utils.writeContents(Utils.join(Main.CURR_FOLDER, filename),
                    (Object) _currblobs.get(filename).content());
        }
        _currbranch = args[1];
        _stage.clear();
    }

    /**Helper function to do untrackcheck given the COMMIT.*/
    private void untrackcheck(Commit commit) {

        for (String file : Utils.plainFilenamesIn(Main.CURR_FOLDER)) {
            if (!_stage.contains(file) && !_currblobs.containsKey(file)
                    && commit.blobnames().contains(file)) {
                System.out.println("There is an untracked"
                        + " file in the way; delete it"
                        + " or add it first.");
                System.exit(0);
            }
        }
    }

    /**Command branch given the ARGS from input.*/
    private void branch(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        } else {
            if (_branches.contains(args[1])) {
                System.out.println("A branch with "
                        + "that name already exists.");
                System.exit(0);
            } else {
                _branches.add(args[1]);
                _splitid.put(args[1], _currcommit);
                Utils.writeContents(Utils.join(BRANCH_FOLDER, args[1]),
                        _currcommit);
            }
        }
    }

    /**Command rm-branch given the ARGS.*/
    private void rmbranch(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        } else {
            if (!_branches.contains(args[1])) {
                System.out.println("A branch"
                        + " with that name does not exist.");
                System.exit(0);
            } else {
                if (_currbranch.equals(args[1])) {
                    System.out.println("Cannot remove"
                            + " the current branch.");
                    System.exit(0);
                } else {
                    _branches.remove(args[1]);
                    Utils.join(BRANCH_FOLDER, args[1]).delete();
                }
            }
        }
    }

    /**Command reset given the ARGS.*/
    private void reset(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        for (String commitid: _commits) {
            if (commitid.substring(0,
                    args[1].length()).equals(args[1])) {
                args[1] = commitid;
            }
        }
        if (!_commits.contains(args[1])) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        if (!_untracked.isEmpty()) {
            System.out.println("There is an untracked file"
                    + " in the way; delete it or add it first.");
            System.exit(0);
        }
        Commit headcommit = Utils.readObject(Utils.join(COMMIT_FOLDER,
                args[1]), Commit.class);
        untrackcheck(headcommit);
        for (String filename : _currblobs.keySet()) {
            Utils.join(Main.CURR_FOLDER, filename).delete();
        }
        _currblobs = headcommit.blobs();
        for (String filename : _currblobs.keySet()) {
            Utils.writeContents(Utils.join(Main.CURR_FOLDER, filename),
                    (Object) _currblobs.get(filename).content());
        }
        Utils.writeContents(Utils.join(BRANCH_FOLDER, _currbranch),
                headcommit.hashcode());
        _currcommit = args[1];
        _stage.clear();
    }

    /**Command merge given ARGS.*/
    private void merge(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        if (!(_stage.isEmpty() && _remove.isEmpty())) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!_branches.contains(args[1])) {
            System.out.println("A branch with that "
                    + "name does not exist.");
            System.exit(0);
        }
        if (_currbranch.equals(args[1])) {
            System.out.println("Cannot merge a branch"
                    + " with itself.");
            System.exit(0);
        }
        String givencommitstr = Utils.readContentsAsString
                (Utils.join(BRANCH_FOLDER, args[1]));
        String splitcommithash = findsplitpoint(_currcommit, givencommitstr);
        if (splitcommithash.equals(givencommitstr)) {
            System.out.println("Given branch is an ancestor "
                    + "of the current branch.");
            System.exit(0);
        }
        Commit currcommit = Utils.readObject(Utils.join(
                COMMIT_FOLDER, _currcommit), Commit.class);
        Commit targetcommit = Utils.readObject(Utils.
                join(COMMIT_FOLDER, givencommitstr), Commit.class);
        Commit splitcommit = Utils.readObject(Utils.
                join(COMMIT_FOLDER, splitcommithash), Commit.class);
        untrackcheck(targetcommit);
        if (splitcommithash.equals(_currcommit)) {
            mergefastforward(targetcommit);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        Boolean conflicted = mergehandle(currcommit, targetcommit, splitcommit);
        if (conflicted) {
            System.out.println("Encountered a merge conflict.");
        }
        String[] commitargs = new String[2];
        commitargs[0] = "commit";
        commitargs[1] = "Merged " + args[1]
                + " into " + _currbranch + '.';
        _merge = true;
        _mergeid = givencommitstr;
        commit(commitargs);
        _merge = false;
    }


    /** Merge fast forward given the TARGETCOMMIT.*/
    private void mergefastforward(Commit targetcommit) {
        String[] resetcommit = new String[2];
        resetcommit[0] = "reset";
        resetcommit[1] = targetcommit.hashcode();
        reset(resetcommit);
        Utils.writeContents(Utils.join(BRANCH_FOLDER,
                _currbranch), _currcommit);
    }

    /**Fields for merge.*/
    /** Merge stagelist.*/
    private ArrayList<String> stagelist;
    /** Merge stagelist.*/
    private ArrayList<String> unchangelist;
    /** Merge removelist.*/
    private ArrayList<String> removelist;
    /** Merge untracklist.*/
    private ArrayList<String> untrackedlist;
    /** Merge conflictlist.*/
    private ArrayList<String> conflictlist;
    /** Marker for merge.*/
    private boolean _merge;
    /** Merge id.*/
    private String _mergeid;
    /**Merge handle to categorize the files into multiple categories.
     * CURRCOMMIT is the current commit, TARGETCOMMIT is the target commit.
     * SPLITCOMMIT the the commit of the split point, return the
     * mark the conflict.*/
    private boolean mergehandle(Commit currcommit, Commit targetcommit,
                                Commit splitcommit) {
        stagelist = new ArrayList<>();
        unchangelist = new ArrayList<>();
        removelist = new ArrayList<>();
        untrackedlist = new ArrayList<>();
        conflictlist = new ArrayList<>();
        commonfilehandle(currcommit, targetcommit, splitcommit);
        List<String> currnotinsplit = currcommit.blobnames();
        currnotinsplit.removeAll(splitcommit.blobnames());
        List<String> targetnotinsplit = targetcommit.blobnames();
        targetnotinsplit.removeAll(splitcommit.blobnames());
        Set<String> currtargetcombo = new HashSet<>(currnotinsplit);
        currtargetcombo.addAll(targetnotinsplit);
        for (String file : currtargetcombo) {
            if (currnotinsplit.contains(file)
                    && !targetnotinsplit.contains(file)) {
                unchangelist.add(file);
            } else {
                if (targetnotinsplit.contains(file)
                        && !currnotinsplit.contains(file)) {
                    stagelist.add(file);
                } else {
                    if (currcommit.blobs().get(file).hashcode().equals
                            (targetcommit.blobs().get(file).hashcode())) {
                        unchangelist.add(file);
                    } else {
                        conflictlist.add(file);
                    }
                }
            }
        }
        mergeforstage(targetcommit);
        mergeforremove();
        mergeforconflict(currcommit, targetcommit);
        return !conflictlist.isEmpty();
    }
    /**Common file merge handle to categorize the files into
     * multiple categories.
     * CURRCOMMIT is the current commit, TARGETCOMMIT is the target commit.
     * SPLITCOMMIT the the commit of the split point, return the
     * mark the conflict.*/
    private void commonfilehandle(Commit currcommit, Commit targetcommit,
                                  Commit splitcommit) {
        for (String file : splitcommit.blobnames()) {
            String splithash = splitcommit.blobs().get(file).hashcode();
            String currhash;
            if (currcommit.blobs().get(file) != null) {
                currhash = currcommit.blobs().get(file).hashcode();
            } else {
                currhash = null;
            }
            String targethash;
            if (targetcommit.blobs().get(file) != null) {
                targethash = targetcommit.blobs().get(file).hashcode();
            } else {
                targethash = null;
            }
            if (currhash != null && targethash != null) {
                if (currhash.equals(splithash)
                        && !targethash.equals(splithash)) {
                    stagelist.add(file);
                } else {
                    if (targethash.equals(splithash)
                            && !currhash.equals(splithash)) {
                        unchangelist.add(file);
                    } else {
                        if (currhash.equals(targethash)) {
                            unchangelist.add(file);
                        } else {
                            conflictlist.add(file);
                        }
                    }
                }

            }
            if (targethash == null && currhash != null) {
                if (currhash.equals(splithash)) {
                    removelist.add(file);
                    untrackedlist.add(file);
                } else {
                    conflictlist.add(file);
                }

            }
            if (currhash == null && targethash != null) {
                if (targethash.equals(splithash)) {
                    unchangelist.add(file);
                } else {
                    conflictlist.add(file);
                }

            }
        }
    }
    /**Handle the files need to be checkout and staged.
     * STAGELIST is the list of files that should be added
     * to the stage, CURRCOMMIT is the current commit,
     * TARGETCOMMIT is the target commit.*/
    private void mergeforstage(Commit targetcommit) {
        for (String file : stagelist) {
            String[] checkoutargs = new String[4];
            checkoutargs[0] = "checkout";
            checkoutargs[1] = targetcommit.hashcode();
            checkoutargs[2] = "--";
            checkoutargs[3] = file;
            checkout(checkoutargs);
            String[] addargs = new String[2];
            addargs[0] = "add";
            addargs[1] = file;
            add(addargs);
        }
    }

    /**Handle all files that need to be removed.
     * REMOVELIST is the arraylist of the files need
     * to be removed.*/
    private void mergeforremove() {
        for (String file : removelist) {
            String[] removeargs = new String[2];
            removeargs[0] = "rm";
            removeargs[1] = file;
            rm(removeargs);
        }
    }

    /**Handle all files that are in conflict.
     * CONFLICTLIST is the arraylist of the files in conflict
     * between the these two commit CURRCOMMIT and TARGETCOMMIT.*/
    private void mergeforconflict(Commit currcommit,
                                  Commit targetcommit) {

        for (String file : conflictlist) {
            String currcontentstr;
            if (currcommit.blobs().get(file) != null) {
                currcontentstr = currcommit.blobs().
                        get(file).contentstr();
            } else {
                currcontentstr = "";
            }

            String targetcontentstr;

            if (targetcommit.blobs().get(file) != null) {
                targetcontentstr = targetcommit.blobs().
                        get(file).contentstr();
            } else {
                targetcontentstr = "";
            }

            String newcontent = "<<<<<<< HEAD\n"
                    + currcontentstr + "=======\n"
                    + targetcontentstr
                    + ">>>>>>>\n";
            Utils.writeContents(Utils.join(Main.CURR_FOLDER, file),
                    newcontent);
            String[] addargs = new String[2];
            addargs[0] = "add";
            addargs[1] = file;
            add(addargs);
        }
    }

    /**Helper function to find split points for two commits.
     * FCOMMIT is the first commit, SCOMMIT is the second commit,
     * return the commit for split point.*/
    private String findsplitpoint(String fcommit, String scommit) {
        if (fcommit.equals(scommit)) {
            return fcommit;
        }
        HashMap<String, Integer> fcommitchain = getcommitchain(fcommit,
                new ArrayList<String>(),
                        new HashMap<String, Integer>());
        HashMap<String, Integer> scommitchain = getcommitchain(
                scommit, new ArrayList<String>(),
                new HashMap<String, Integer>());
        HashSet<String> allcommits = new HashSet<>(fcommitchain.keySet());
        allcommits.addAll(scommitchain.keySet());

        int minilevel = MAXVALUE;
        String split = "";
        for (String commit : allcommits) {
            if (fcommitchain.containsKey(commit)
                    && scommitchain.containsKey(commit)) {
                int thislevel = fcommitchain.get(commit)
                        + scommitchain.get(commit);
                if (thislevel < minilevel) {
                    minilevel = thislevel;
                    split = commit;
                }
            }
        }
        return split;
    }


    /**Get the chain of the commit from the input to the initial commit.
     * The COMMITHASH is the hashcode for the commit,
     * CHAIN is the commit history, return commit chain, LEVELS is the
     * commit history combined with level counts.*/
    private HashMap<String, Integer> getcommitchain(String commithash,
                                              Collection<String> chain,
                                              HashMap<String, Integer> levels) {
        Deque<String> parents = new LinkedList<>();
        Deque<Integer> levelchain = new LinkedList<>();
        int level = 0;
        parents.addLast(commithash);
        levelchain.addLast(level);
        while (!parents.isEmpty()) {
            String thiscommit = parents.removeFirst();
            int thislevel = levelchain.removeFirst();

            Commit commit = Utils.readObject(Utils.join(COMMIT_FOLDER,
                    thiscommit), Commit.class);
            if (!chain.contains(thiscommit)) {
                chain.add(thiscommit);
                if (levels.get(thiscommit) != null
                        && levels.get(thiscommit) < thislevel) {
                    levels.put(thiscommit, thislevel);
                } else {
                    levels.put(thiscommit, thislevel);
                }
            }
            if (!commit.parentid().equals("")) {
                parents.addLast(commit.parentid());
                levelchain.addLast(thislevel + 1);
            }
            if (commit.merge()) {
                parents.addLast(commit.mergeid());
                levelchain.addLast(thislevel + 1);
            }
        }
        return levels;
    }

    /**Get the chain of the commit from the input to the initial commit
     * in a recursive way. The COMMITHASH is the hashcode for
     * the commit, CHAIN is the commit history, return the commit chain.*/
    private Collection<String> recurgetcommitchain(String commithash,
                                                   Collection<String> chain) {
        if (!_commits.contains(commithash)) {
            return chain;
        } else {
            Commit commit = Utils.readObject(Utils.join(COMMIT_FOLDER,
                    commithash), Commit.class);
            chain.add(commithash);
            Collection<String> chainpath = recurgetcommitchain(
                    commit.parentid(), chain);
            if (commit.merge()) {
                Collection<String> chainmergepath =
                        recurgetcommitchain(commit.mergeid(), chain);
                if (chainmergepath.size() <= chainpath.size()) {
                    return chainmergepath;
                }
            }
            return chainpath;
        }
    }
}
