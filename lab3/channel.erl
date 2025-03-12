-module(channel).
-export([stop/1, start/1, handle/2]).


start(ChannelAtom) ->
    Pid = genserver:start(ChannelAtom, [], fun handle/2),
    Pid. 

stop(ChannelAtom) ->
    genserver:stop(ChannelAtom).

handle(Users, {join, Client}) ->
    io:format("Channel handling join request: Users=~p, Client=~p~n", [Users, Client]),
    case lists:member(Client, Users) of 
        true  -> 
            {reply, {error, client_already_joined, "Client already joined" }, Users};
        false -> 
            {reply, ok, [Client | Users]}
    end;

handle(Users, {leave, Client}) ->
    io:format("Channel handling leave request: Users=~p, Client=~p~n", [Users, Client]),
    case lists:member(Client, Users) of 
        true  -> 
            {reply, ok, lists:delete(Client, Users)};
        false -> 
            {reply, {error, client_not_a_member, "Client is not a member" }, Users}
    end;

handle(Users, {broadcast_message, ChannelAtom, Msg, Nick, Client}) ->
    io:format("Channel broadcasting message: Msg=~p, Nick=~p~n", [Msg, Nick]),
    
    % Send the message to all clients in the channel (except the sender)
    lists:foreach(fun(ClientInChannel) ->
                        % Skip the sender (the client who sent the message)
                        case ClientInChannel =/= Client of
                            true -> 
                                % Send message to all other clients
                                genserver:request(Client, {message_receive, ChannelAtom, Nick, Msg}),
                                io:format("Sending message to ~p: ~p~n", [ClientInChannel, Msg]);
                            false -> 
                                io:format("Skipping message to sender: ~p~n", [ClientInChannel])
                        end
                    end, Users),
    
    {reply, ok, Users}.

% get_string_from_pid() -> This was pro coding but took to long for 500ms
%     atom_to_list(element(2,process_info(self(), registered_name))).
