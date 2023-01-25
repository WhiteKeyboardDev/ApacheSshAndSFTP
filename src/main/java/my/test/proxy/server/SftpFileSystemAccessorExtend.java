package my.test.proxy.server;

import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.sftp.server.DirectoryHandle;
import org.apache.sshd.sftp.server.FileHandle;
import org.apache.sshd.sftp.server.SftpFileSystemAccessor;
import org.apache.sshd.sftp.server.SftpSubsystemProxy;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.FileLock;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.security.Principal;
import java.util.*;

public class SftpFileSystemAccessorExtend implements SftpFileSystemAccessor {
    @Override
    public Path resolveLocalFilePath(SftpSubsystemProxy subsystem, Path rootDir, String remotePath) throws IOException, InvalidPathException {
        return SftpFileSystemAccessor.super.resolveLocalFilePath(subsystem, rootDir, remotePath);
    }

    @Override
    public LinkOption[] resolveFileAccessLinkOptions(SftpSubsystemProxy subsystem, Path file, int cmd, String extension, boolean followLinks) throws IOException {
        return SftpFileSystemAccessor.super.resolveFileAccessLinkOptions(subsystem, file, cmd, extension, followLinks);
    }

    @Override
    public NavigableMap<String, Object> resolveReportedFileAttributes(SftpSubsystemProxy subsystem, Path file, int flags, NavigableMap<String, Object> attrs, LinkOption... options) throws IOException {
        return SftpFileSystemAccessor.super.resolveReportedFileAttributes(subsystem, file, flags, attrs, options);
    }

    @Override
    public void applyExtensionFileAttributes(SftpSubsystemProxy subsystem, Path file, Map<String, byte[]> extensions, LinkOption... options) throws IOException {
        SftpFileSystemAccessor.super.applyExtensionFileAttributes(subsystem, file, extensions, options);
    }

    @Override
    public void putRemoteFileName(SftpSubsystemProxy subsystem, Path path, Buffer buf, String name, boolean shortName) throws IOException {
        SftpFileSystemAccessor.super.putRemoteFileName(subsystem, path, buf, name, shortName);
    }

    @Override
    public SeekableByteChannel openFile(SftpSubsystemProxy subsystem, FileHandle fileHandle, Path file, String handle, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        return SftpFileSystemAccessor.super.openFile(subsystem, fileHandle, file, handle, options, attrs);
    }

    @Override
    public FileLock tryLock(SftpSubsystemProxy subsystem, FileHandle fileHandle, Path file, String handle, Channel channel, long position, long size, boolean shared) throws IOException {
        return SftpFileSystemAccessor.super.tryLock(subsystem, fileHandle, file, handle, channel, position, size, shared);
    }

    @Override
    public void syncFileData(SftpSubsystemProxy subsystem, FileHandle fileHandle, Path file, String handle, Channel channel) throws IOException {
        SftpFileSystemAccessor.super.syncFileData(subsystem, fileHandle, file, handle, channel);
    }

    @Override
    public void closeFile(SftpSubsystemProxy subsystem, FileHandle fileHandle, Path file, String handle, Channel channel, Set<? extends OpenOption> options) throws IOException {
        SftpFileSystemAccessor.super.closeFile(subsystem, fileHandle, file, handle, channel, options);
    }

    @Override
    public DirectoryStream<Path> openDirectory(SftpSubsystemProxy subsystem, DirectoryHandle dirHandle, Path dir, String handle) throws IOException {
        return SftpFileSystemAccessor.super.openDirectory(subsystem, dirHandle, dir, handle);
    }

    @Override
    public void closeDirectory(SftpSubsystemProxy subsystem, DirectoryHandle dirHandle, Path dir, String handle, DirectoryStream<Path> ds) throws IOException {
        SftpFileSystemAccessor.super.closeDirectory(subsystem, dirHandle, dir, handle, ds);
    }

    @Override
    public Map<String, ?> readFileAttributes(SftpSubsystemProxy subsystem, Path file, String view, LinkOption... options) throws IOException {
        return SftpFileSystemAccessor.super.readFileAttributes(subsystem, file, view, options);
    }

    @Override
    public void setFileAttribute(SftpSubsystemProxy subsystem, Path file, String view, String attribute, Object value, LinkOption... options) throws IOException {
        SftpFileSystemAccessor.super.setFileAttribute(subsystem, file, view, attribute, value, options);
    }

    @Override
    public UserPrincipal resolveFileOwner(SftpSubsystemProxy subsystem, Path file, UserPrincipal name) throws IOException {
        return SftpFileSystemAccessor.super.resolveFileOwner(subsystem, file, name);
    }

    @Override
    public void setFileOwner(SftpSubsystemProxy subsystem, Path file, Principal value, LinkOption... options) throws IOException {
        SftpFileSystemAccessor.super.setFileOwner(subsystem, file, value, options);
    }

    @Override
    public GroupPrincipal resolveGroupOwner(SftpSubsystemProxy subsystem, Path file, GroupPrincipal name) throws IOException {
        return SftpFileSystemAccessor.super.resolveGroupOwner(subsystem, file, name);
    }

    @Override
    public void setGroupOwner(SftpSubsystemProxy subsystem, Path file, Principal value, LinkOption... options) throws IOException {
        SftpFileSystemAccessor.super.setGroupOwner(subsystem, file, value, options);
    }

    @Override
    public void setFilePermissions(SftpSubsystemProxy subsystem, Path file, Set<PosixFilePermission> perms, LinkOption... options) throws IOException {
        SftpFileSystemAccessor.super.setFilePermissions(subsystem, file, perms, options);
    }

    @Override
    public void setFileAccessControl(SftpSubsystemProxy subsystem, Path file, List<AclEntry> acl, LinkOption... options) throws IOException {
        SftpFileSystemAccessor.super.setFileAccessControl(subsystem, file, acl, options);
    }

    @Override
    public void createDirectory(SftpSubsystemProxy subsystem, Path path) throws IOException {
        SftpFileSystemAccessor.super.createDirectory(subsystem, path);
    }

    @Override
    public void createLink(SftpSubsystemProxy subsystem, Path link, Path existing, boolean symLink) throws IOException {
        SftpFileSystemAccessor.super.createLink(subsystem, link, existing, symLink);
    }

    @Override
    public String resolveLinkTarget(SftpSubsystemProxy subsystem, Path link) throws IOException {
        return SftpFileSystemAccessor.super.resolveLinkTarget(subsystem, link);
    }

    @Override
    public void renameFile(SftpSubsystemProxy subsystem, Path oldPath, Path newPath, Collection<CopyOption> opts) throws IOException {
        SftpFileSystemAccessor.super.renameFile(subsystem, oldPath, newPath, opts);
    }

    @Override
    public void copyFile(SftpSubsystemProxy subsystem, Path src, Path dst, Collection<CopyOption> opts) throws IOException {
        SftpFileSystemAccessor.super.copyFile(subsystem, src, dst, opts);
    }

    @Override
    public void removeFile(SftpSubsystemProxy subsystem, Path path, boolean isDirectory) throws IOException {
        SftpFileSystemAccessor.super.removeFile(subsystem, path, isDirectory);
    }
}
