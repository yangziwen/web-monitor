package io.github.yangziwen.webmonitor.command;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import io.github.yangziwen.webmonitor.Server;

@Parameters(separators = "=", commandDescription = "run the server")
public class ServerCommand implements Command {

    @Parameter(names = {"-h", "--help"}, description = "print this message", help = true)
    public boolean help;

    @Parameter(names = {"-ip", "--ip-address"}, description = "specify the ip that accepted")
    public String ipAddress = "0.0.0.0";

    @Parameter(names = {"-p", "--port"}, description = "specify the port to listen")
    public Integer port = 8050;

    @Parameter(names = {"-l", "--static-location"}, description = "specify the folder the static files")
    public String staticLocation = "/static";

    @Override public void invoke(JCommander commander) {
        if (help) {
            commander.usage(name());
            return;
        }
        Server.run(ipAddress, port, staticLocation);
    }

}
