package com.wonjune.buildcache_jenkins_plugin;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Environment;
import hudson.model.EnvironmentSpecific;
import hudson.model.TaskListener;
import hudson.model.Descriptor;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildWrapper;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import hudson.model.Run;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class CachedBuildWrapper extends SimpleBuildWrapper {
  private final String cacheServerAddress;
  private final int cacheServerPort;

  @DataBoundConstructor
  public CachedBuildWrapper(String cacheServerAddress, int cacheServerPort) {
    this.cacheServerAddress = cacheServerAddress;
    this.cacheServerPort = cacheServerPort;
  }

  @Override
  public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
    try {
      // 캐싱 서버와 연결
      // 예를 들어, HTTP 서버인 경우:
      URL cacheServerUrl = new URL("http://" + cacheServerAddress + ":" + cacheServerPort + "/build.zip");
      HttpURLConnection connection = (HttpURLConnection) cacheServerUrl.openConnection();
      connection.setRequestMethod("GET");

      if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
        listener.getLogger().println("캐싱된 빌드 파일이 존재합니다. 재사용합니다.");

        if (workspace != null) {
          // 캐싱된 빌드 파일을 워크스페이스로 다운로드
          workspace.child("build.zip").copyFrom(cacheServerUrl);
          workspace.child("build.zip").unzip(workspace);
        } else {
          listener.getLogger().println("워크스페이스를 찾을 수 없습니다. 캐싱된 빌드 파일을 사용할 수 없습니다.");
        }
      } else {
        listener.getLogger().println("캐싱된 빌드 파일이 없습니다.");
      }
    } catch (IOException | InterruptedException e) {
      listener.getLogger().println("빌드 파일 캐싱 중 에러가 발생했습니다: " + e.getMessage());
    }
  }

  @Extension
  public static final class DescriptorImpl extends BuildWrapperDescriptor {
      public DescriptorImpl() {
        super(CachedBuildWrapper.class);
      }

      @Override
      public String getDisplayName() {
        return "Enable build cache";
      }

      @Override
      public CachedBuildWrapper newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        String cacheServerAddress = formData.getString("cacheServerAddress");
        int cacheServerPort = formData.getInt("cacheServerPort");
        return new CachedBuildWrapper(cacheServerAddress, cacheServerPort);
      }

      @Override
      public boolean isApplicable(AbstractProject<?, ?> aClass) {
        return true; // 이 플러그인은 모든 프로젝트 유형에 적용할 수 있다고 가정합니다.
      }

      public FormValidation doCheckCacheServerAddress(@QueryParameter String value) {
        if (value.isEmpty()) {
          return FormValidation.error("Cache server address를 입력해 주세요.");
        }
        return FormValidation.ok();
      }

      public FormValidation doCheckCacheServerPort(@QueryParameter int value) {
        if (value <= 0) {
          return FormValidation.error("Cache server port를 올바르게 입력해 주세요.");
        }
        return FormValidation.ok();
      }
  }

}
