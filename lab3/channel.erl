-module(channel).
-export([stop/1, start/1, handle/2]).


start(ChannelAtom) ->
    Pid = genserver:start(ChannelAtom, [], fun handle/2),
    Pid. 

stop(ChannelAtom) ->
    genserver:stop(ChannelAtom).

handle(Users, {join, Client}) ->
    case lists:member(Client, Users) of 
        true  -> 
            {reply, {error, user_already_joined, "user already joined" }, Users};
        false -> 
            {reply, ok, [Client | Users]}
    end;

handle(Users, {leave, Client}) ->
    case lists:member(Client, Users) of 
        false -> 
            {reply, {error, user_not_joined, "Client not member!"}, Users};
        true -> 
            {reply, ok, lists:delete(Client, Users)} %Make this quit 
    end;

handle(Users, {message_send, Msg, Client, Nick}) ->
    io:format("Client: ~p~n", [Client]),
    io:format("message: ~p~n", [Msg]),
    io:format("Nick: ~p~n", [Nick]),
    io:format("Users: ~p~n", [lists:delete(Client, Users)]),
    io:format("Users2: ~p~n", [Users]),
    case lists:member(Client, Users) of
        false ->
            {reply, {error, user_not_joined, "User not a member!"}, Users};
        true ->
        lists:foreach(fun(X) -> genserver:request(X, {message_receive, get_string_from_pid() , Nick, Msg}) end, 
                        lists:delete(Client, Users)),
        {reply, ok, Users}    
    end.

get_string_from_pid() ->
    atom_to_list(element(2,process_info(self(), registered_name))).
