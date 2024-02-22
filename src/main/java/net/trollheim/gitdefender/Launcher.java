package net.trollheim.gitdefender;


import net.trollheim.gitdefender.actions.ActionConfig;
import net.trollheim.gitdefender.actions.safestore.SafeStore;
import net.trollheim.gitdefender.cmd.ActionConfigProperties;
import net.trollheim.gitdefender.discovery.GithubDiscoveryService;
import org.apache.commons.cli.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.util.Properties;

public class Launcher {


    private static Options options(){
        Options options = new Options();
        options.addOption(Option.builder().option("m").longOpt("mode").hasArg().desc("mode discovery, backup or decrypt").required().build());
        options.addOption(Option.builder().option("c").longOpt("config").hasArg().desc("Config file path").required().build());
        options.addOption(Option.builder().option("r").longOpt("repo-name").hasArg().desc("full repository name").required().build());
        options.addOption(Option.builder().option("o").longOpt("output-file").hasArg().desc("output file location").required().build());

        return options;
    }


    public static void main(String[] args) throws ParseException {
        Provider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options(), args);

        var config = cmd.getOptionValue("c");
        ActionConfig properties = loadProperties(config, cmd);
        var mode = cmd.getOptionValue("m");

        switch (mode){
            case "discovery" -> discoveryAction(properties);

            case "backup" -> backupAction(cmd, properties);

        }


    }

    private static void backupAction(CommandLine cmd, ActionConfig properties) {

        GithubDiscoveryService discoveryService = new GithubDiscoveryService(properties);
        var reponame = cmd.getOptionValue("r");
        if (reponame == null) {
            reponame = properties.get(Constants.REPO_NAME_FIELD);
        }
        var output = cmd.getOptionValue("o");
        if (output != null) {
            properties.put(Constants.OUTPUT_FILE_LOCATION_FIELD, output);
        }

        SafeStore safestore = new SafeStore();

        discoveryService.execute(reponame, repo -> {safestore.execute(repo, properties); return "ok";});

    }

    private static void discoveryAction(ActionConfig properties) {
        GithubDiscoveryService discoveryService = new GithubDiscoveryService(properties);
        var repoOwner = properties.get(Constants.REPO_OWNER_FIELD);
        var isOrg = "ORG".equals(properties.get(Constants.REPO_TYPE_FIELD));
        try {
            discoveryService.getRepos(repoOwner, isOrg).stream().forEach(s -> System.out.println(s.getFullName()+" "+s.getName()+" "));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ActionConfig loadProperties(String filename, CommandLine cmd){
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

}
