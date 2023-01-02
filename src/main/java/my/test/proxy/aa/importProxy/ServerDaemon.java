package my.test.proxy.aa.importProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.sshd.common.AttributeRepository;
import org.apache.sshd.common.io.*;
import org.apache.sshd.common.kex.KexProposalOption;
import org.apache.sshd.common.session.ConnectionService;
import org.apache.sshd.common.session.ReservedSessionMessagesHandler;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

public class ServerDaemon {
    private static final Logger logger = LogManager.getLogger(ServerDaemon.class);
    private static int step = 0;

    private static class Daemon extends Thread {
        private SshServer sshd;
        private ClientDaemon client;

        public Daemon() {
            this.client = new ClientDaemon();

            try {
                this.client.create();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            this.sshd = SshServer.setUpDefaultServer();
            this.sshd.setPort(2022);
            this.sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
            CustomShellFactory shellFactory = new CustomShellFactory(this.client);
            this.sshd.setShellFactory(shellFactory);
            this.sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
                @Override
                public boolean authenticate(String username, String password, ServerSession session)
                        throws PasswordChangeRequiredException, AsyncAuthException {
                    IoSession iosess = session.getIoSession();
                    IoService iosvc = iosess.getService();

                    iosvc.setIoServiceEventListener(new IoServiceEventListener() {
                        @Override
                        public void connectionAccepted(IoAcceptor acceptor, SocketAddress local, SocketAddress remote, SocketAddress service) throws IOException {
                            logger.info(String.format(" ---------------- %d. connectionAccepted", ++(ServerDaemon.step)));
                            IoServiceEventListener.super.connectionAccepted(acceptor, local, remote, service);
                        }

                        @Override
                        public void abortAcceptedConnection(IoAcceptor acceptor, SocketAddress local,
                                                            SocketAddress remote, SocketAddress service, Throwable reason) throws IOException {
                            logger.info(String.format(" ---------------- %d. abortAcceptedConnection", ++(ServerDaemon.step)));
                            IoServiceEventListener.super.abortAcceptedConnection(acceptor, local, remote, service, reason);
                        }

                        @Override
                        public void abortEstablishedConnection(IoConnector connector, SocketAddress local,
                                                               AttributeRepository context, SocketAddress remote, Throwable reason)
                                throws IOException {
                            logger.info(String.format(" ---------------- %d. abortEstablishedConnection", ++(ServerDaemon.step)));
                            IoServiceEventListener.super.abortEstablishedConnection(connector, local, context, remote, reason);
                        }

                        @Override
                        public void connectionEstablished(IoConnector connector, SocketAddress local,
                                                          AttributeRepository context, SocketAddress remote) throws IOException {
                            logger.info(String.format(" ---------------- %d. connectionEstablished", ++(ServerDaemon.step)));
                        }
                    });

                    session.setReservedSessionMessagesHandler(new ReservedSessionMessagesHandler() {
                        @Override
                        public void handleDebugMessage(Session session, Buffer buffer) throws Exception {
                            logger.info(String.format(" ---------------- %d. handleDebugMessage", ++(ServerDaemon.step)));
                            ReservedSessionMessagesHandler.super.handleDebugMessage(session, buffer);
                        }

                        @Override
                        public void handleIgnoreMessage(Session session, Buffer buffer) throws Exception {
                            logger.info(String.format(" ---------------- %d. handleIgnoreMessage", ++(ServerDaemon.step)));
                            ReservedSessionMessagesHandler.super.handleIgnoreMessage(session, buffer);
                        }

                        @Override
                        public boolean handleUnimplementedMessage(Session session, int cmd, Buffer buffer)
                                throws Exception {
                            logger.info(String.format(" ---------------- %d. handleUnimplementedMessage", ++(ServerDaemon.step)));
                            return ReservedSessionMessagesHandler.super.handleUnimplementedMessage(session, cmd, buffer);
                        }

                        @Override
                        public IoWriteFuture sendIdentification(Session session, String version,
                                                                List<String> extraLines) throws Exception {
                            logger.info(String.format(" ---------------- %d. sendIdentification", ++(ServerDaemon.step)));
                            return ReservedSessionMessagesHandler.super.sendIdentification(session, version, extraLines);
                        }

                        @Override
                        public IoWriteFuture sendKexInitRequest(Session session,
                                                                Map<KexProposalOption, String> proposal, Buffer packet) throws Exception {
                            logger.info(String.format(" ---------------- %d. sendKexInitRequest", ++(ServerDaemon.step)));
                            return ReservedSessionMessagesHandler.super.sendKexInitRequest(session, proposal, packet);
                        }

                        @Override
                        public boolean sendReservedHeartbeat(ConnectionService service) throws Exception {
                            logger.info(String.format(" ---------------- %d. sendReservedHeartbeat", ++(ServerDaemon.step)));
                            return ReservedSessionMessagesHandler.super.sendReservedHeartbeat(service);
                        }
                    });

                    return true;
                }
            });
            this.sshd.addSessionListener(new SessionListener() {
                @Override
                public void sessionEstablished(Session session) {
                    logger.info(String.format(" ---------------- %d. sessionEstablished", ++(ServerDaemon.step)));
                    SessionListener.super.sessionEstablished(session);
                }

                @Override
                public void sessionCreated(Session session) {
                    logger.info(String.format(" ---------------- %d. sessionCreated", ++(ServerDaemon.step)));
                    SessionListener.super.sessionCreated(session);
                }

                @Override
                public void sessionEvent(Session session, Event event) {
                    logger.info(String.format(" ---------------- %d. sessionEvent", ++(ServerDaemon.step)));
                    SessionListener.super.sessionEvent(session, event);

                    logger.info(" ================ EVENT: " + event.toString());

                    if (event == Event.Authenticated) {

                    }
                }

                @Override
                public void sessionException(Session session, Throwable t) {
                    logger.info(String.format(" ---------------- %d. sessionException", ++(ServerDaemon.step)));
                    SessionListener.super.sessionException(session, t);
                }

                @Override
                public void sessionPeerIdentificationSend(Session session, String version, List<String> extraLines) {
                    logger.info(String.format(" ---------------- %d. sessionPeerIdentificationSend", ++(ServerDaemon.step)));
                    SessionListener.super.sessionPeerIdentificationSend(session, version, extraLines);
                }

                @Override
                public void sessionPeerIdentificationLine(Session session, String line, List<String> extraLines) {
                    logger.info(String.format(" ---------------- %d. sessionPeerIdentificationLine", ++(ServerDaemon.step)));
                    SessionListener.super.sessionPeerIdentificationLine(session, line, extraLines);
                }

                @Override
                public void sessionPeerIdentificationReceived(Session session, String version, List<String> extraLines) {
                    logger.info(String.format(" ---------------- %d. sessionPeerIdentificationReceived", ++(ServerDaemon.step)));
                    SessionListener.super.sessionPeerIdentificationReceived(session, version, extraLines);
                }

                @Override
                public void sessionDisconnect(Session session, int reason, String msg, String language, boolean initiator) {
                    logger.info(String.format(" ---------------- %d. sessionDisconnect", ++(ServerDaemon.step)));
                    SessionListener.super.sessionDisconnect(session, reason, msg, language, initiator);
                }

                @Override
                public void sessionClosed(Session session) {
                    logger.info(String.format(" ---------------- %d. sessionClosed", ++(ServerDaemon.step)));
                    SessionListener.super.sessionClosed(session);
                }

                @Override
                public void sessionNegotiationStart(Session session, Map<KexProposalOption, String> clientProposal,
                                                    Map<KexProposalOption, String> serverProposal) {
                    logger.info(String.format(" ---------------- %d. sessionNegotiationStart", ++(ServerDaemon.step)));
                    SessionListener.super.sessionNegotiationStart(session, clientProposal, serverProposal);
                }

                @Override
                public void sessionNegotiationEnd(Session session, Map<KexProposalOption, String> clientProposal,
                                                  Map<KexProposalOption, String> serverProposal, Map<KexProposalOption, String> negotiatedOptions,
                                                  Throwable reason) {
                    logger.info(String.format(" ---------------- %d. sessionNegotiationEnd", ++(ServerDaemon.step)));
                    SessionListener.super.sessionNegotiationEnd(session, clientProposal, serverProposal, negotiatedOptions, reason);
                }

                @Override
                public void sessionNegotiationOptionsCreated(Session session, Map<KexProposalOption, String> proposal) {
                    logger.info(String.format(" ---------------- %d. sessionNegotiationOptionsCreated", ++(ServerDaemon.step)));
                    SessionListener.super.sessionNegotiationOptionsCreated(session, proposal);
                }
            });
        }

        @Override
        public void run() {
            try {
                this.sshd.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean isHealthy() {
            return !this.sshd.isClosed();
        }

        public void kill() {
            try {
                this.sshd.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

//      BasicConfigurator.configure();
        Daemon sshd = new Daemon();
        sshd.start();


        while (sshd.isHealthy()) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                sshd.kill();
                System.exit(1);
            }
        }
    }
}
