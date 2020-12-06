package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Qindan Zhu
 */
public class Main {
    /** Working directory.*/
    static final File CURR_FOLDER = new File(".");
    /** Main directory.*/
    static final File MAIN_FOLDER = Utils.join(CURR_FOLDER, ".gitlet");
    /** Record for resuming repo.*/
    static final File RECORD = Utils.join(MAIN_FOLDER, "gitlet");
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length < 1) {
            System.out.println("Please enter a command.");
        } else {
            if (!Arrays.asList(_validcommands).contains(args[0])) {
                System.out.println(" No command with that name exists.");
            } else {
                if (!MAIN_FOLDER.exists()) {
                    _repo = new Repo();
                } else {
                    _repo = Repo.fromFile();
                }
                _repo.process(args);
                saverepo();
            }
        }
        System.exit(0);
    }

    /** Save repo by overwriting record.*/
    private static void saverepo() {
        Utils.writeObject(RECORD, _repo);
    }

    /** Repo.*/
    private static Repo _repo;

    /** Valid command.*/
    private static String[] _validcommands = {"init",
        "add", "commit", "rm", "log", "global-log",
        "find", "status", "checkout", "branch",
        "rm-branch", "reset", "merge"};

}
