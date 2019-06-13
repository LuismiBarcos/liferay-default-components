package com.liferay.fragments.collector;

import com.liferay.fragment.contributor.FragmentCollectionContributorTracker;
import com.liferay.fragment.model.FragmentEntry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.*;
import java.util.Map;

/**
 * @author Luis Miguel Barcos
 */

@Component(
        immediate = true
)
public class FragmentsCollector {
    @Reference
    private FragmentCollectionContributorTracker
            _fragmentCollectionContributorTracker;

    @Activate
    public void start() {
        final String configFile = "config/config.properties";

        final String path = this.getDirectoryToSaveFragments(configFile);
        System.out.println("Content -> " + path);

        final Map<String, FragmentEntry> fragmentEntries = _fragmentCollectionContributorTracker.getFragmentEntries();

        fragmentEntries.forEach(
            (key, value) -> {
                System.out.println("key: " + key + " value: " + value.getName());
                this.createFiles(path, value);
            }
        );
    }

    private String getDirectoryToSaveFragments(String fileName) {
        final InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        String path = "";

        if (is != null) {
            String line;
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(is))){
                while((line = reader.readLine()) != null) {
                    path = line.split("=")[1];
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        return path;
    }

    private void createFiles(String path, FragmentEntry fragmentInfo) {
        final String folderPath = this.createFolder(path + fragmentInfo.getName());
        if(folderPath != null) {
            this.createHtmlFile(folderPath, fragmentInfo);
            this.createCssFile(folderPath, fragmentInfo);
            this.createJsFile(folderPath, fragmentInfo);
        } else {
            System.out.println("Folder not created for " + fragmentInfo.getName() + ". Exists?");
        }
    }

    private String createFolder(String path) {
        final boolean folderCreated = new File(path).mkdir();
        return folderCreated ? path : null;
    }

    private void createHtmlFile(String path, FragmentEntry fragmentInfo) {
        this.createFile(
                path,
                "index",".html",
                fragmentInfo.getHtml());
    }

    private void createCssFile(String path, FragmentEntry fragmentInfo) {
        this.createFile(
                path,
                "styles",".css",
                fragmentInfo.getCss());
    }

    private void createJsFile(String path, FragmentEntry fragmentInfo) {
        this.createFile(
                path,
                "main",".js",
                fragmentInfo.getJs());
    }

    private void createFile(String path, String fileName, String extension, String fileContent) {
        try (FileOutputStream file = new FileOutputStream(path + "/" + fileName + extension)){
            file.write(fileContent.getBytes());
            file.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}