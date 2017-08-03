package ar.com.utn.proyecto.qremergencias.ws.config;

import io.undertow.attribute.StoredResponse;
import io.undertow.security.api.SecurityContext;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.StoredResponseHandler;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.websockets.WebSocketProtocolHandshakeHandler;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;

import static ar.com.utn.proyecto.qremergencias.ws.config.ProxyConfig.MessagingEmitterHandler.messagingEmitterHandler;
import static io.undertow.Handlers.path;
import static io.undertow.Handlers.proxyHandler;
import static io.undertow.Handlers.resource;
import static io.undertow.Handlers.websocket;
import static io.undertow.server.handlers.ResponseCodeHandler.HANDLE_404;
import static io.undertow.util.Headers.ACCEPT_LANGUAGE;
import static io.undertow.util.Headers.CONTENT_TYPE;
import static io.undertow.util.LocaleUtils.getLocalesFromHeader;

@Configuration
@AutoConfigureAfter(ServerPropertiesAutoConfiguration.class)
@SuppressWarnings("PMD")
public class ProxyConfig {

    private static final String HOST = "localhost:";

    private final ServerProperties serverProperties;

    @Autowired
    public ProxyConfig(final ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Bean
    public UndertowEmbeddedServletContainerFactory embeddedServletContainerFactory() {
        final UndertowEmbeddedServletContainerFactory factory = new UndertowEmbeddedServletContainerFactory();

        factory.addBuilderCustomizers(builder -> {
            LoadBalancingProxyClient loadBalancer;
            try {
                final Integer port = serverProperties.getPort();
                final String protocol = serverProperties.getSsl() == null ? "http" : "https";
                loadBalancer = new LoadBalancingProxyClient()
                        .addHost(new URI(protocol + HOST + port))
                        .setConnectionsPerThread(20);
            } catch (final URISyntaxException exception) {
                throw new RuntimeException(exception);
            }

            final WebSocketProtocolHandshakeHandler websocket = websocket((exchange, channel) -> {
                channel.getReceiveSetter().set(new AbstractReceiveListener() {
                    @Override
                    protected void onFullTextMessage(final WebSocketChannel channel,
                                                     final BufferedTextMessage message) {
                        final String messageData = message.getData();
                        channel.getPeerConnections()
                                .forEach(session -> WebSockets.sendText(messageData, session, null));
                    }
                });
                channel.resumeReceives();
            });

            builder.addHttpListener(8083, "0.0.0.0",
                    path(messagingEmitterHandler(
                            new StoredResponseHandler(proxyHandler(loadBalancer, 30000, HANDLE_404)), websocket))
                            .addPrefixPath("/echo", websocket)
                            .addPrefixPath("/monitor",
                                    resource(new ClassPathResourceManager(ProxyConfig.class.getClassLoader()))
                                            .addWelcomeFiles("monitor.html")));

        });
        return factory;
    }

    static class MessagingEmitterHandler implements HttpHandler {

        private final HttpHandler next;
        private final WebSocketProtocolHandshakeHandler wsHandler;

        private MessagingEmitterHandler(final HttpHandler next, final WebSocketProtocolHandshakeHandler wsHandler) {
            this.next = next;
            this.wsHandler = wsHandler;
        }

        protected static MessagingEmitterHandler messagingEmitterHandler(final HttpHandler next,
                                                               final WebSocketProtocolHandshakeHandler wsHandler) {
            return new MessagingEmitterHandler(next, wsHandler);
        }

        @Override
        public void handleRequest(final HttpServerExchange exchange) throws Exception {
            final StringBuilder sb = new StringBuilder(500);

            final SecurityContext sc = exchange.getSecurityContext();
            sb.append("\n----------------------------REQUEST---------------------------\n");
            sb.append("URI=").append(exchange.getRequestURI()).append('\n');
            sb.append("characterEncoding=").append(exchange.getRequestHeaders().get(Headers.CONTENT_ENCODING))
                    .append('\n');
            sb.append("contentLength=").append(exchange.getRequestContentLength()).append('\n');
            sb.append("contentType=").append(exchange.getRequestHeaders().get(CONTENT_TYPE)).append('\n');
            addAuthentication(sb, sc);

            final Map<String, Cookie> cookies = exchange.getRequestCookies();
            if (cookies != null) {
                for (final Map.Entry<String, Cookie> entry : cookies.entrySet()) {
                    final Cookie cookie = entry.getValue();
                    sb.append("cookie=").append(cookie.getName()).append("=").append(cookie.getValue()).append('\n');
                }
            }
            for (final HeaderValues header : exchange.getRequestHeaders()) {
                for (final String value : header) {
                    sb.append("header=").append(header.getHeaderName()).append("=").append(value).append('\n');
                }
            }
            sb.append("locale=").append(getLocalesFromHeader(exchange.getRequestHeaders().get(ACCEPT_LANGUAGE)))
                    .append('\n');
            sb.append("method=").append(exchange.getRequestMethod()).append('\n');
            final Map<String, Deque<String>> pnames = exchange.getQueryParameters();
            for (final Map.Entry<String, Deque<String>> entry : pnames.entrySet()) {
                final String pname = entry.getKey();
                sb.append("parameter=");
                sb.append(pname);
                sb.append('=');
                final Iterator<String> pvalues = entry.getValue().iterator();
                while (pvalues.hasNext()) {
                    sb.append(pvalues.next());
                    if (pvalues.hasNext()) {
                        sb.append(", ");
                    }
                }
                sb.append('\n');
            }
            sb.append("protocol=").append(exchange.getProtocol()).append('\n');
            sb.append("queryString=").append(exchange.getQueryString()).append('\n');
            sb.append("remoteAddr=").append(exchange.getSourceAddress()).append('\n');
            sb.append("remoteHost=").append(exchange.getSourceAddress().getHostName()).append('\n');
            sb.append("scheme=").append(exchange.getRequestScheme()).append('\n');
            sb.append("host=").append(exchange.getRequestHeaders().getFirst(Headers.HOST)).append('\n');
            sb.append("serverPort=").append(exchange.getDestinationAddress().getPort()).append('\n');

            exchange.addExchangeCompleteListener((exchange1, nextListener) -> {
                // Log post-service information
                sb.append("--------------------------RESPONSE--------------------------\n");
                addAuthentication(sb, sc);
                sb.append("contentLength=").append(exchange1.getResponseContentLength()).append('\n');
                sb.append("contentType=").append(exchange1.getResponseHeaders().getFirst(CONTENT_TYPE)).append('\n');
                final Map<String, Cookie> cookies1 = exchange1.getResponseCookies();
                if (cookies1 != null) {
                    for (final Cookie cookie : cookies1.values()) {
                        sb.append("cookie=").append(cookie.getName()).append('=').append(cookie.getValue())
                                .append("; domain=").append(cookie.getDomain())
                                .append("; path=").append(cookie.getPath()).append('\n');
                    }
                }
                for (final HeaderValues header : exchange1.getResponseHeaders()) {
                    for (final String value : header) {
                        sb.append("header=").append(header.getHeaderName()).append("=").append(value).append('\n');
                    }
                }
                sb.append("status=").append(exchange1.getStatusCode()).append('\n');
                final String storedResponse = StoredResponse.INSTANCE.readAttribute(exchange1);
                if (storedResponse != null) {
                    sb.append("body=\n");
                    sb.append(storedResponse);
                }

                sb.append("==============================================================");

                nextListener.proceed();
                wsHandler.getPeerConnections()
                        .forEach(session -> WebSockets.sendText(sb.toString(), session, null));
            });
            next.handleRequest(exchange);
        }

        private void addAuthentication(final StringBuilder sb, final SecurityContext sc) {
            if (sc != null) {
                if (sc.isAuthenticated()) {
                    sb.append("authType=").append(sc.getMechanismName()).append('\n');
                    sb.append("principle=").append(sc.getAuthenticatedAccount().getPrincipal()).append('\n');
                } else {
                    sb.append("authType=none" + '\n');
                }
            }
        }

    }

}
