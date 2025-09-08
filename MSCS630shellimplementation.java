//Umesh Dhakal
//09/01/2025
//Shell implementation
//Deliverable 1
//Shell implementation in Java


//imports
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

//Job structure
class Job {
    int jobId;
    Process process;
    String command;

    Job(int jobId, Process process, String command) {
        this.jobId = jobId;
        this.process = process;
        this.command = command;
    }
}
//Shellimplementation class
public class MSCS630shellimplementation {

    private static Map<Integer, Job> jobs = new ConcurrentHashMap<>();
    private static int jobCounter = 1;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String currentDir = System.getProperty("user.dir");

        while (true) {
            System.out.print("java_shell> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            // Background execution
            boolean background = input.endsWith("&");
            if (background) input = input.substring(0, input.length() - 1).trim();

            String[] tokens = input.split("\\s+");
            String command = tokens[0];

            try {
		    //Switch 
                switch (command) {
			//Change directory
                    case "cd":
                        if (tokens.length < 2) {
                            System.out.println("cd: missing argument");
                        } else {
                            File dir = new File(tokens[1]);
                            if (dir.exists() && dir.isDirectory()) {
                                currentDir = dir.getAbsolutePath();
                                System.setProperty("user.dir", currentDir);
                            } else {
                                System.out.println("cd: directory does not exist");
                            }
                        }
                        break;
			//Path of the source file

                    case "pwd":
                        System.out.println(currentDir);
                        break;
			//Exit the shell
                    case "exit":
                        System.exit(0);
                        break;

                    case "echo":
                        for (int i = 1; i < tokens.length; i++)
                            System.out.print(tokens[i] + " ");
                        System.out.println();
                        break;
			//Clear the shell

                    case "clear":
                        System.out.print("\033[H\033[2J");
                        System.out.flush();
                        break;
			//List of all the file in the directory
                    case "ls":
                        File folder = new File(currentDir);
                        String[] files = folder.list();
                        if (files != null) {
                            for (String f : files)
                                System.out.println(f);
                        }
                        break;
			//Making the new directory

                    case "mkdir":
                        if (tokens.length < 2) {
                            System.out.println("mkdir: missing directory name");
                        } else {
                            File newDir = new File(currentDir, tokens[1]);
                            if (!newDir.mkdir())
                                System.out.println("mkdir: could not create directory");
                        }
                        break;
			//REmoving the directory if exist
                    case "rmdir":
                        if (tokens.length < 2) {
                            System.out.println("rmdir: missing directory name");
                        } else {
                            File dirToRemove = new File(currentDir, tokens[1]);
                            if (!dirToRemove.delete())
                                System.out.println("rmdir: could not remove directory");
                        }
                        break;
			//Creating the file

                    case "touch":
                        if (tokens.length < 2) {
                            System.out.println("touch: missing filename");
                        } else {
                            File f = new File(currentDir, tokens[1]);
                            f.createNewFile();
                        }
                        break;
			//Copying the existing file

                    case "cat":
                        if (tokens.length < 2) {
                            System.out.println("cat: missing filename");
                        } else {
                            File f = new File(currentDir, tokens[1]);
                            if (f.exists()) {
                                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                                    String line;
                                    while ((line = br.readLine()) != null)
                                        System.out.println(line);
                                }
                            } else {
                                System.out.println("cat: file does not exist");
                            }
                        }
                        break;
			// Removing the file if exist
                    case "rm":
                        if (tokens.length < 2) {
                            System.out.println("rm: missing filename");
                        } else {
                            File f = new File(currentDir, tokens[1]);
                            if (!f.delete())
                                System.out.println("rm: could not delete file");
                        }
                        break;
			//killing the jobs
                    case "kill":
                        if (tokens.length < 2) {
                            System.out.println("kill: missing pid");
                        } else {
                            int pid = Integer.parseInt(tokens[1]);
                            boolean found = false;
                            for (Job job : jobs.values()) {
                                if (job.process.isAlive() && job.process.pid() == pid) {
                                    job.process.destroy();
                                    jobs.remove(job.jobId);
                                    found = true;
                                    break;
                                }
                            }
                            if (!found)
                                System.out.println("kill: no such process");
                        }
                        break;
			//Process management
                    case "jobs":
                        for (Job job : jobs.values()) {
                            String status = job.process.isAlive() ? "Running" : "Done";
                            System.out.println("[" + job.jobId + "] " + job.process.pid() + " " + status + " " + job.command);
                        }
                        break;

                    case "fg":
                        if (tokens.length < 2) {
                            System.out.println("fg: missing job id");
                        } else {
                            int jid = Integer.parseInt(tokens[1]);
                            Job job = jobs.get(jid);
                            if (job != null) {
                                job.process.waitFor();
                                jobs.remove(jid);
                            } else {
                                System.out.println("fg: job " + jid + " not found");
                            }
                        }
                        break;

                    case "bg":
                        System.out.println("bg command not fully implemented yet in Java shell");
                        break;

                    default:
                        // External command
                        ProcessBuilder pb = new ProcessBuilder(tokens);
                        pb.directory(new File(currentDir));
                        if (background) {
                            Process p = pb.start();
                            jobs.put(jobCounter, new Job(jobCounter, p, input));
                            System.out.println("[" + jobCounter + "] " + p.pid());
                            jobCounter++;
                        } else {
                            Process p = pb.inheritIO().start();
                            p.waitFor();
                        }
                        break;
                }
            } catch (Exception e) {
                System.out.println(command + ": " + e.getMessage());
            }
        }
    }
}

