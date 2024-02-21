package net.trollheim.gitdefender.actions;

import net.trollheim.gitdefender.model.GitRepo;
import org.kohsuke.github.GHRepository;

public interface DefenderAction {

    void execute(GHRepository repo, ActionConfig config);

}
