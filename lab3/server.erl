-module(server).
-export([start/1, messageLoop/0]).%,stop/1]).


% Start a new server process with the given name
% Do not change the signature of this function.
start(ServerAtom) ->
    Pid = spawn(?MODULE, messageLoop, []),
    Pid ! ServerAtom.
    % TODO Implement function
    % - Spawn a new process which waits for a message, handles it, then loops infinitely
    % - Register this process to ServerAtom
    % - Return the process ID
    % not_implemented.
 
% Stop the server process registered to the given name,
% together with any other associated processes
% stop(ServerAtom) ->
%     % TODO Implement function
%     % Return ok
%     not_implemented.

messageLoop() ->
    receive 
        started -> io:format("Server started!");
        _ -> io:format("HI")
    end.