package org.betonquest.betonquest.modules.web.updater.source.implementations;

import org.betonquest.betonquest.modules.versioning.Version;
import org.betonquest.betonquest.modules.web.ContentSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the {@link NexusReleaseAndDevelopmentSource}.
 */
class NexusReleaseAndDevelopmentSourceTest {
    /**
     * The path to the root page for a specific Nexus.
     */
    public static final String API_URL = "https://betonquest.org/nexus";

    @SuppressWarnings({"PMD.JUnitTestContainsTooManyAsserts"})
    @Test
    void testNexusReleaseSource() throws IOException {
        final Path filePathShadedPage1 = Path.of("src/test/resources/modules/web/updater/nexusShadedPage1.json");
        final Path filePathShadedPage2 = Path.of("src/test/resources/modules/web/updater/nexusShadedPage2.json");
        final String apiUrlShadedPage1 = API_URL + NexusReleaseAndDevelopmentSource.SERVICE_REST_V_1 + NexusReleaseAndDevelopmentSource.SEARCH_URL;
        final String apiUrlShadedPage2 = API_URL + NexusReleaseAndDevelopmentSource.SERVICE_REST_V_1 + NexusReleaseAndDevelopmentSource.SEARCH_URL + NexusReleaseAndDevelopmentSource.CONTINUATION_TOKEN + "2";

        final ContentSource contentSource = url ->
                switch (url.toString()) {
                    case apiUrlShadedPage1 -> Files.readString(filePathShadedPage1);
                    case apiUrlShadedPage2 -> Files.readString(filePathShadedPage2);
                    default -> throw new IOException("Unexpected URL: " + url);
                };
        final NexusReleaseAndDevelopmentSource source = new NexusReleaseAndDevelopmentSource(API_URL, contentSource);

        final Map<Version, String> versions = source.getReleaseVersions();

        assertEquals(3, versions.size(), "Expected two versions from getReleaseVersions");
        final String url1 = versions.get(new Version("1.12.4"));
        assertEquals(API_URL + "/repository/betonquest/org/betonquest/betonquest/1.12.4/betonquest-1.12.4-shaded.jar", url1,
                "The download URL is not correct");
        final String url2 = versions.get(new Version("1.12.5"));
        assertEquals(API_URL + "/repository/betonquest/org/betonquest/betonquest/1.12.5/betonquest-1.12.5-shaded.jar", url2,
                "The download URL is not correct");
        final String url3 = versions.get(new Version("1.12.6"));
        assertEquals(API_URL + "/repository/betonquest/org/betonquest/betonquest/1.12.6/betonquest-1.12.6-shaded.jar", url3,
                "The download URL is not correct");
    }

    @SuppressWarnings({"PMD.JUnitTestContainsTooManyAsserts"})
    @Test
    void testNexusDevelopmentSource() throws IOException {
        final Path filePathShadedPage1 = Path.of("src/test/resources/modules/web/updater/nexusShadedPage1.json");
        final Path filePathShadedPage2 = Path.of("src/test/resources/modules/web/updater/nexusShadedPage2.json");
        final String apiUrlShadedPage1 = API_URL + NexusReleaseAndDevelopmentSource.SERVICE_REST_V_1 + NexusReleaseAndDevelopmentSource.SEARCH_URL;
        final String apiUrlShadedPage2 = API_URL + NexusReleaseAndDevelopmentSource.SERVICE_REST_V_1 + NexusReleaseAndDevelopmentSource.SEARCH_URL + NexusReleaseAndDevelopmentSource.CONTINUATION_TOKEN + "2";

        final Path filePathPom1 = Path.of("src/test/resources/modules/web/updater/nexusPom1.xml");
        final Path filePathPom2 = Path.of("src/test/resources/modules/web/updater/nexusPom2.xml");
        final String apiUrlPom1 = API_URL + "/repository/betonquest/org/betonquest/betonquest/2.0.0-SNAPSHOT/betonquest-2.0.0-20221230.085132-398.pom";
        final String apiUrlPom2 = API_URL + "/repository/betonquest/org/betonquest/betonquest/1.12.7-SNAPSHOT/betonquest-1.12.7-20221210.125708-379.pom";

        final ContentSource contentSource = url ->
                switch (url.toString()) {
                    case apiUrlShadedPage1 -> Files.readString(filePathShadedPage1);
                    case apiUrlShadedPage2 -> Files.readString(filePathShadedPage2);
                    case apiUrlPom1 -> Files.readString(filePathPom1);
                    case apiUrlPom2 -> Files.readString(filePathPom2);
                    default -> throw new IOException("Unexpected URL: " + url);
                };
        final NexusReleaseAndDevelopmentSource source = new NexusReleaseAndDevelopmentSource(API_URL, contentSource);

        final Map<Version, String> versions = source.getDevelopmentVersions();

        assertEquals(2, versions.size(), "Expected two versions from getReleaseVersions");
        final String url1 = versions.get(new Version("2.0.0-DEV-495"));
        assertEquals(apiUrlPom1.replace(".pom", "-shaded.jar"), url1, "The download URL is not correct");
        final String url2 = versions.get(new Version("1.12.7-DEV-379"));
        assertEquals(apiUrlPom2.replace(".pom", "-shaded.jar"), url2, "The download URL is not correct");
    }
}
