package api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import controller.Logic;
import database.DatabaseWrapper;
import model.Game;
import model.Score;
import model.User;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.ArrayList;
import java.util.HashMap;

@Path("/api")
public class Api {

    private class Status {
        private static final int CREATED = 201;
        private static final int BAD_REQUEST = 400;
        private static final int SERVER_ERROR = 500;
    }

    private ResponseBuilder ok() {
        return Response.ok();
    }

    private ResponseBuilder created() {
        return Response.status(Status.CREATED);
    }

    private ResponseBuilder badReq() {
        return Response.status(Status.BAD_REQUEST);
    }

    private ResponseBuilder error() {
        return Response.status(Status.SERVER_ERROR);
    }

    private Response withCors(ResponseBuilder resp) {
        return resp.header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS").build();
    }

    @GET //"GET-Request" gør at vi kan forspørge en specifik data
    @Produces(MediaType.APPLICATION_JSON)
    public String getClichedMessage() {
        // Return some cliched textual content
        return "Hello World!";
    }

    @OPTIONS
    @Path("/login/")
    public Response login() {
        return withCors(ok());
    }

    @POST //"POST-request" er ny data vi kan indtaste for at logge ind.
    @Path("/login/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(String data) {
        ResponseBuilder result;
        try {

            User user = new Gson().fromJson(data, User.class);

            HashMap<String, Integer> hashMap = Logic.authenticateUser(user.getUsername(), user.getPassword());

            switch (hashMap.get("code")) {
                case 0:
                    result = badReq().entity("{\"message\":\"Wrong username or password\"}");
                    break;
                case 1:
                    result = badReq().entity("{\"message\":\"Wrong username or password\"}");
                    break;
                case 2:
                    result = ok().entity("{\"message\":\"Login successful\", \"userid\":" + hashMap.get("userid") + "}");
                    break;
                default:
                    result = error()
                            .entity("{\"message\":\"Unknown error. Please contact Henrik Thorn at: henrik@itkartellet.dk\"}");
            }

        } catch (JsonSyntaxException | NullPointerException e) {
            e.printStackTrace();
            result = badReq().entity("{\"message\":\"Error in JSON\"}");
        }

        return withCors(result);
    }

    @GET //"GET-request"
    @Path("/users/") //USER-path - identifice det inden for metoden
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {

        ArrayList<User> users = Logic.getUsers();

        return withCors(ok().entity(new Gson().toJson(users)));
    }

    /*
    @DELETE //DELETE-request fjernelse af data (bruger): Slet bruger
    @Path("/users/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("userid") int userId) {

        int deleteUser = Logic.deleteUser(userId);

        if (deleteUser == 1) {
            return Response
                    .status(200)
                    .entity("{\"message\":\"User was deleted\"}")
                    .header("Access-Control-Allow-Headers", "*")
                    .build();
        } else {
            return Response
                    .status(400)
                    .entity("{\"message\":\"Failed. User was not deleted\"}")
                    .header("Access-Control-Allow-Headers", "*")
                    .build();
        }

    }
*/

    @POST //POST-request: Ny data der skal til serveren; En ny bruger oprettes
    @Path("/users/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(String data) {
        ResponseBuilder result;
        User user;

        try {
            user = new Gson().fromJson(data, User.class);

            user.setType(1);

            boolean createdUser = Logic.createUser(user);

            result = createdUser ?
                    ok().entity("{\"message\":\"User was created\"}") :
                    badReq().entity("{\"message\":\"Username or email already exists\"}");

        } catch (JsonSyntaxException | NullPointerException e) {
            e.printStackTrace();
            result = badReq().entity("{\"message\":\"Error in JSON\"}");
        }

        return withCors(result);
    }

    @OPTIONS
    @Path("/users/{userId}")
    public Response getUser() {
        return withCors(ok());
    }

    @GET //"GET-request"
    @Path("/users/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("userId") int userId) {
        ResponseBuilder result;
        User user = Logic.getUser(userId);
        //udprint/hent/identificer af data omkring spillere
        result = user == null ?
                ok().entity("{\"message\":\"User was not found\"}") :
                ok().entity(new Gson().toJson(user));

        return withCors(result);
    }

    @OPTIONS
    @Path("/games/")
    public Response createGame() {
        return withCors(ok());
    }

    @POST //POST-request: Nyt data; nyt spil oprettes
    @Path("/games/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGame(String json) {
        ResponseBuilder result;
        try {
            Game game = Logic.createGame(new Gson().fromJson(json, Game.class));

            result = game == null ?
                    badReq().entity("{\"message\":\"something went wrong\"}") :
                    created().entity(new Gson().toJson(game));
        } catch (JsonSyntaxException | NullPointerException e) {
            e.printStackTrace();
            result = badReq().entity("{\"message\":\"Error in JSON\"}");
        }

        return withCors(result);
    }

    @PUT
    @Path("/games/join/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response joinGame(String json) {
        ResponseBuilder result;
        try {
            Game game = new Gson().fromJson(json, Game.class);

            result = Logic.joinGame(game) ?
                    created().entity("{\"message\":\"Game was joined\"}") :
                    badReq().entity("{\"message\":\"Game closed\"}");
        } catch (JsonSyntaxException | NullPointerException e) {
            e.printStackTrace();
            result = badReq().entity("{\"message\":\"Error in JSON\"}");
        }

        return withCors(result);
    }

    @OPTIONS
    @Path("/games/join/")
    public Response joinGame() {
        return withCors(ok());
    }

    @PUT
    @Path("/games/start/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startGame(String json) {
        ResponseBuilder result;
        try {
            Game game = Logic.startGame(new Gson().fromJson(json, Game.class));

            result = game == null ? badReq().entity("something went wrong") : ok().entity(new Gson().toJson(game));

        } catch (JsonSyntaxException | NullPointerException e) {
            e.printStackTrace();
            result = badReq().entity("{\"message\":\"Error in JSON\"}");
        }

        return withCors(result);
    }

    @OPTIONS
    @Path("/games/{gameid}")
    public Response deleteGame() {
        return withCors(ok());
    }

    @DELETE //DELETE-request fjernelse af data(spillet slettes)
    @Path("/games/{gameid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteGame(@PathParam("gameid") int gameId) {
        int deleteGame = Logic.deleteGame(gameId);

        return withCors(
                deleteGame == 1 ?
                        ok().entity("{\"message\":\"Game was deleted\"}") :
                        badReq().entity("{\"message\":\"Failed. Game was not deleted\"}")
        );
    }

    @GET //"GET-request"
    @Path("/game/{gameid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGame(@PathParam("gameid") int gameid) {
        Game game = Logic.getGame(gameid);
        return withCors(ok().entity(new Gson().toJson(game)));
    }

    @OPTIONS
    @Path("/scores/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHighscoreOpts() {
        return withCors(ok());
    }

    @GET //"GET-request"
    @Path("/scores/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHighscore() {
        return withCors(ok().entity(new Gson().toJson(Logic.getHighscore())));
    }

    /*
    Getting games by userid
     */
    @GET //"GET-request"
    @Path("/games/{userid}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGamesByUserID(@PathParam("userid") int userId) {

        ArrayList<Game> games = Logic.getGames(DatabaseWrapper.GAMES_BY_ID, userId);

        return withCors(ok().entity(new Gson().toJson(games)));
    }

    /*
    Getting games by game status and user id
     */
    @GET //"GET-request"
    @Path("/games/{status}/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGamesByStatusAndUserID(@PathParam("status") String status, @PathParam("userid") int userId) {
        ArrayList<Game> games = null;
        switch (status) {
            case "pending":
                games = Logic.getGames(DatabaseWrapper.PENDING_BY_ID, userId);
                break;
            case "open":
                games = Logic.getGames(DatabaseWrapper.OPEN_BY_ID, userId);
                break;
            case "finished":
                games = Logic.getGames(DatabaseWrapper.COMPLETED_BY_ID, userId);
                break;
        }

        return withCors(ok().entity(new Gson().toJson(games)));
    }

    //Gets all games where the user is invited
    @GET
    @Path("/games/opponent/{userid}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGamesInvitedByID(@PathParam("userid") int userId) {
        ArrayList<Game> games = Logic.getGames(DatabaseWrapper.PENDING_INVITED_BY_ID, userId);

        return withCors(ok().entity(new Gson().toJson(games)));
    }

    //Gets all games hosted by the user
    @GET
    @Path("/games/host/{userid}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGamesHostedByID(@PathParam("userid") int userId) {
        ArrayList<Game> games = Logic.getGames(DatabaseWrapper.PENDING_HOSTED_BY_ID, userId);

        return withCors(ok().entity(new Gson().toJson(games)));
    }

    /*
    Getting a list of all open games
     */
    @GET //"GET-request"
    @Path("/games/open/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOpenGames() {
        ArrayList<Game> games = Logic.getGames(DatabaseWrapper.OPEN_GAMES, 0);

        return withCors(ok().entity(new Gson().toJson(games)));
    }

    /*
    Getting all scores by user id
    Used for showing all finished games and scores for the user
     */
    @GET //"GET-request"
    @Path("/scores/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScoresByUserID(@PathParam("userid") int userid) {
        ArrayList<Score> score = Logic.getScoresByUserID(userid);

        return withCors(ok().entity(new Gson().toJson(score)));
    }
}