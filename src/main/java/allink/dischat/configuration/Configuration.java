package allink.dischat.configuration;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
@NoArgsConstructor
public class Configuration {
    private String token;
    private String owner;
    private String guildId;

    public static class Manager {
        private static final Path CONFIGURATION_PATH = Path.of("config.yml");
        private final Yaml yaml = new Yaml(new Constructor(Configuration.class));
        @Getter
        private Configuration configuration;

        public Manager() throws IOException {
            if (!Files.exists(CONFIGURATION_PATH)) {
                final Class<? extends Manager> clazz = this.getClass();
                final ClassLoader classLoader = clazz.getClassLoader();
                final InputStream defaultConfiguration = classLoader.getResourceAsStream("default-config.yml");

                if (defaultConfiguration == null) {
                    throw new RuntimeException("Default configuration resource is null!");
                }

                Files.copy(defaultConfiguration, CONFIGURATION_PATH);
            }

            loadFromFile();
        }

        public void loadFromFile() throws IOException {
            this.configuration = yaml.load(Files.readString(CONFIGURATION_PATH));
        }
    }
}
