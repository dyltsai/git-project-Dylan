import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Git {

    public static void initGitRepo() throws IOException {
        File gitDir = new File("./git");
        File objectsDir = new File("./git/objects");
        File indexFile = new File("./git/index");

        if (gitDir.exists() && objectsDir.exists() && indexFile.exists()) {
            System.out.println("Git Repository already exists");
            return;
        }

        if (!gitDir.exists()) {
            gitDir.mkdir();
        }

        if (!objectsDir.exists()) {
            objectsDir.mkdir();
        }

        if (!indexFile.exists()) {
            indexFile.createNewFile();
        }

    }

    public static void initGitRepoTester() throws IOException {

        File gitDir = new File("./git");
        File objectsDir = new File("./git/objects");
        File indexFile = new File("./git/index");

        initGitRepo();

        boolean repoCreated = gitDir.exists() && objectsDir.exists() && indexFile.exists();

        // delete files in directories first
        indexFile.delete();
        objectsDir.delete();
        gitDir.delete();

        boolean checkDeletedFiles = !(gitDir.exists() && objectsDir.exists() && indexFile.exists());

        if (repoCreated && checkDeletedFiles) {
            System.out.println("Created repo and deleted files.");
        } else {
            if (repoCreated) {
                System.out.println("Created repo.");
            } else {
                System.out.println("Did not create repo.");
            }

            if (checkDeletedFiles) {
                System.out.println("Deleted files.");
            } else {
                System.out.println("Did not delete files.");
            }
        }

    }

    public static String sha1(Path file) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] byteArr = md.digest(Files.readAllBytes(file));
        BigInteger n = new BigInteger(1, byteArr);
        String hash = n.toString(16);
        while (hash.length() < 40) {
            hash = "0" + hash;
        }

        return hash;
    }

    public static void createNewBlob(Path path) throws IOException, NoSuchAlgorithmException {

        Path hash = Paths.get(".git/objects/" + sha1(path));

        if (Files.exists(hash)) {
            Files.delete(hash);
        }

        Files.copy(path, hash);

        BufferedReader br = new BufferedReader(new FileReader((".git/index")));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(".git/index", true))) {
            writer.write(path.getFileName() + " " + sha1(path));
            writer.newLine();
        }
    }

    public static void createNewBlobTester(Path path) throws NoSuchAlgorithmException, IOException {
        createNewBlob(path);

        String hash = sha1(path);
        Path blobPath = Paths.get(".git/objects/" + hash);
        if (!Files.exists(blobPath)) {
            System.out.println("blob doesn't exist: " + blobPath);
        } else {
            System.out.println("blob exists: " + blobPath);
        }

        String hash1 = sha1(path);
        String hash2 = sha1(blobPath);

        if (hash1.equals(hash2)) {
            System.out.println("the content of file is the same");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(".git/index"))) {
            String line;
            boolean indexLineExists = false;
            while ((line = br.readLine()) != null) {
                if (line.equals(path.getFileName() + " " + hash)) {
                    indexLineExists = true;
                    break;
                }
            }

            if (!indexLineExists) {
                System.out.println("no index entry: " + path.getFileName());
                return;
            }
        }

        Files.deleteIfExists(path);
        Files.deleteIfExists(blobPath);

        if (!Files.exists(path) && !Files.exists(blobPath)) {
            System.out.println("files were deleted");
        }

        System.out.println("everything was created (blob + index entry)");

    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        // initGitRepo();
        // initGitRepoTester();

        // sha1 tester
        Path testFile = Paths.get(".git/testFile.txt");
        testFile.toFile().createNewFile();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(("testFile.txt")));
            writer.write("hello world");
            writer.close();
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
        System.out.println("hash: " + sha1(testFile));
        System.out.println("expected hash: " + "2aae6c35c94fcfb415dbe95f408b9ce91ee846ed");

        // testing blob
        createNewBlob(testFile);
        createNewBlobTester(testFile);
    }

}