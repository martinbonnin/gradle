package org.gradle.launcher.cli;

import com.google.common.collect.Lists;
import net.rubygrapefruit.platform.WindowsRegistry;
import org.gradle.api.GradleException;
import org.gradle.api.internal.file.IdentityFileResolver;
import org.gradle.api.internal.file.temp.TemporaryFileProvider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.internal.jvm.Jvm;
import org.gradle.internal.jvm.inspection.CachingJvmMetadataDetector;
import org.gradle.internal.jvm.inspection.DefaultJvmMetadataDetector;
import org.gradle.internal.jvm.inspection.JvmInstallationMetadata;
import org.gradle.internal.jvm.inspection.JvmMetadataDetector;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.jvm.toolchain.internal.AsdfInstallationSupplier;
import org.gradle.jvm.toolchain.internal.CurrentInstallationSupplier;
import org.gradle.jvm.toolchain.internal.InstallationSupplier;
import org.gradle.jvm.toolchain.internal.IntellijInstallationSupplier;
import org.gradle.jvm.toolchain.internal.JabbaInstallationSupplier;
import org.gradle.jvm.toolchain.internal.JavaInstallationRegistry;
import org.gradle.jvm.toolchain.internal.JvmInstallationMetadataComparator;
import org.gradle.jvm.toolchain.internal.LinuxInstallationSupplier;
import org.gradle.jvm.toolchain.internal.MavenToolchainsInstallationSupplier;
import org.gradle.jvm.toolchain.internal.OsXInstallationSupplier;
import org.gradle.jvm.toolchain.internal.SdkmanInstallationSupplier;
import org.gradle.jvm.toolchain.internal.WindowsInstallationSupplier;
import org.gradle.process.internal.ExecFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Daemon JVM auto-detection implementation for use in the launcher.
 */
public class DaemonJvmSelector {
    private final JavaInstallationRegistry registry;
    private final JvmMetadataDetector detector;

    public DaemonJvmSelector(
        ProviderFactory providerFactory,
        ExecFactory execHandleFactory,
        TemporaryFileProvider temporaryFileProvider,
        WindowsRegistry windowsRegistry
    ) {
        List<InstallationSupplier> installationSuppliers = defaultInstallationSuppliers(providerFactory, execHandleFactory, windowsRegistry);

        this.registry = new JavaInstallationRegistry(installationSuppliers, null, OperatingSystem.current());
        this.detector = new CachingJvmMetadataDetector(
            new DefaultJvmMetadataDetector(execHandleFactory, temporaryFileProvider));
    }

    public JvmInstallationMetadata getDaemonJvmInstallation() {
        // TODO: Make the constraint more configurable.
        // But this works!
        Optional<JvmInstallationMetadata> installation = getInstallation(it -> it.getLanguageVersion().isJava11Compatible());
        if (!installation.isPresent()) {
            throw new GradleException("Somehow can't find an installation. Maybe the current JVM is <8 and we can detect others?");
        }
        return installation.get();
    }

    private Optional<JvmInstallationMetadata> getInstallation(Predicate<? super JvmInstallationMetadata> criteria) {
        return registry.listInstallations().stream()
            .map(detector::getMetadata)
            .filter(JvmInstallationMetadata::isValidInstallation)
            .filter(criteria)
            .min(new JvmInstallationMetadataComparator(Jvm.current()));
    }

    // TODO: We should standardize our installation suppliers across
    // AvailableJavaHomes, this, and PlatformJvmServices.
    private static List<InstallationSupplier> defaultInstallationSuppliers(ProviderFactory providerFactory, ExecFactory execFactory, WindowsRegistry windowsRegistry) {
        return Lists.newArrayList(
            new AsdfInstallationSupplier(providerFactory),
            new CurrentInstallationSupplier(providerFactory),
            new IntellijInstallationSupplier(providerFactory, new IdentityFileResolver()),
            new JabbaInstallationSupplier(providerFactory),
            new LinuxInstallationSupplier(providerFactory),
            new MavenToolchainsInstallationSupplier(providerFactory, new IdentityFileResolver()),
            new OsXInstallationSupplier(execFactory, providerFactory, OperatingSystem.current()),
            new SdkmanInstallationSupplier(providerFactory),
            new WindowsInstallationSupplier(windowsRegistry, OperatingSystem.current(), providerFactory)
        );
    }
}
