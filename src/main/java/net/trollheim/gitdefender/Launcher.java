package net.trollheim.gitdefender;


import net.trollheim.gitdefender.actions.ActionConfig;
import net.trollheim.gitdefender.actions.safestore.EcdhUtils;
import net.trollheim.gitdefender.actions.safestore.SafeStore;
import net.trollheim.gitdefender.cmd.ActionConfigProperties;
import net.trollheim.gitdefender.discovery.GithubDiscoveryService;
import org.apache.commons.cli.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHRepository;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.GCMParameterSpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.util.Base64;
import java.util.Properties;

public class Launcher {




    static KeyPair keyPair;

    private static Options options(){
        Options options = new Options();
        options.addOption(Option.builder().option("m").longOpt("mode").hasArg().desc("mode discovery, backup or decrypt").required().build());
        options.addOption(Option.builder().option("c").longOpt("config").hasArg().desc("Config file path").required().build());

        return options;
    }


    public static void main(String[] args) throws ParseException {
        Provider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options(), args);

        var config = cmd.getOptionValue("c");
        ActionConfig properties = loadProperties(config);
        var mode = cmd.getOptionValue("m");
        switch (mode){
            case "discovery" -> discoveryAction(properties);

            case "backup" -> backupAction(properties);
        }


    }

    private static void backupAction(ActionConfig properties) {

        GithubDiscoveryService discoveryService = new GithubDiscoveryService(properties);
        var reponame = properties.get(Constants.REPO_NAME_FIELD);
        SafeStore safestore = new SafeStore();

        discoveryService.execute(reponame, repo -> {safestore.execute(repo, properties); return "ok";});

    }

    private static void discoveryAction(ActionConfig properties) {
        GithubDiscoveryService discoveryService = new GithubDiscoveryService(properties);
        var reponame = properties.get(Constants.REPO_OWNER_FIELD);
        System.out.println(reponame);
        try {
            discoveryService.getRepos(reponame, false).stream().forEach(s -> System.out.println(s.getFullName()+" "+s.getName()+" "));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ActionConfig loadProperties(String filename){
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(filename));
            return new ActionConfigProperties(properties);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }


    public static void main_old(String[] args) {
        try {

            var token = "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";
            CredentialsProvider creds = new UsernamePasswordCredentialsProvider("wtcichon@gmail.com", token);//
            Git git = Git.cloneRepository()
                    .setCredentialsProvider(creds)
                    .setURI("https://github.com/wtcichon/test-repo.git")
                    .setDirectory(new File("test"))
                    .setRemote("origin")
                    .call();
//
////        Git.open(new File("test")).remoteSetUrl().setRemoteName("origin").setRemoteUri(new URIish("https://github.com/wtcichon/test-repo.git")).call();
//
//        var result = Git.open(new File("test")).push().setCredentialsProvider(creds).call();
//        result.forEach(c -> System.out.println(c.getMessages()));
////        Git git = new Git("s").remoteSetUrl().setRemoteName().setRemoteUri().call();
////            git.push().setCredentialsProvider()
        } catch (Exception e){

            e.printStackTrace();
        }
    }


}
