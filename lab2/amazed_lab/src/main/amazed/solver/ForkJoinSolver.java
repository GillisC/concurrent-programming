package amazed.solver;

import amazed.maze.Maze;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <code>ForkJoinSolver</code> implements a solver for
 * <code>Maze</code> objects using a fork/join multi-thread
 * depth-first search.
 * <p>
 * Instances of <code>ForkJoinSolver</code> should be run by a
 * <code>ForkJoinPool</code> object.
 */


public class ForkJoinSolver
    extends SequentialSolver
{
    // This is a global variable for all solvers so we can detect if the heart has been found
    static AtomicBoolean heartFound = new AtomicBoolean(false);
    protected static ConcurrentSkipListSet<Integer> visited;
    protected Map<Integer, Integer> predecessor;
    protected Stack<Integer> frontier;

    /**
     * Creates a solver that searches in <code>maze</code> from the
     * start node to a goal.
     *
     * @param maze   the maze to be searched
     */
    public ForkJoinSolver(Maze maze)
    {
        super(maze);
    }

    /**
     * Creates a solver that searches in <code>maze</code> from the
     * start node to a goal, forking after a given number of visited
     * nodes.
     *
     * @param maze        the maze to be searched
     * @param forkAfter   the number of steps (visited nodes) after
     *                    which a parallel task is forked; if
     *                    <code>forkAfter &lt;= 0</code> the solver never
     *                    forks new tasks
     */
    public ForkJoinSolver(Maze maze, int forkAfter)
    {
        this(maze);
        this.forkAfter = forkAfter;
    }

    // This is the constructor used when creating new solvers
    public ForkJoinSolver(Maze maze, int start, ConcurrentSkipListSet<Integer> visited, Map<Integer, Integer> predecessor)
    {
        this(maze);
        // We override the following attributes when creating a solver
        this.start = start;
        this.visited = visited;
        this.predecessor = predecessor;
    }

    /**
     * Searches for and returns the path, as a list of node
     * identifiers, that goes from the start node to a goal node in
     * the maze. If such a path cannot be found (because there are no
     * goals, or all goals are unreacheable), the method returns
     * <code>null</code>.
     *
     * @return   the list of node identifiers from the start node to a
     *           goal node in the maze; <code>null</code> if such a path cannot
     *           be found.
     */
    @Override
    public List<Integer> compute()
    {
        return parallelSearch();
    }

    private List<Integer> parallelSearch()
    {
        int player = maze.newPlayer(start);
        frontier.push(start);

        while (!frontier.isEmpty()) 
        {   
            int current = frontier.pop();
            
            // If the heart has been found stop current worker
            if (heartFound.get()) 
            {
                System.out.println("Stopping current thread...");
                return null;
            }
            

            // If we reach the heart
            if (maze.hasGoal(current)) 
            {
                System.out.println("Found the heart!");
                heartFound.set(true);
                
                maze.move(player, current);
                visited.add(current);
                
                return pathFromTo(maze.start(), current);
            }

            // If we reach an unvisited tile
            if (!visited.contains(current)) {
                maze.move(player, current);
                visited.add(current);

                Set<Integer> neighbors = maze.neighbors(current);
                int unvisited = getUnvisitedNeighbors(current, visited);
                if (unvisited >= 2) 
                {  
                    // Place to store solvers and their result
                    List<ForkJoinSolver> solvers = new ArrayList<>();
                    List<Integer> result = null;

                    for (int nb: neighbors) 
                    {   
                        if (!visited.contains(nb))
                        {
                            predecessor.put(nb, current);
                            // Create a new solver starting on this neighbor
                            ForkJoinSolver solver = new ForkJoinSolver(this.maze, nb, this.visited, this.predecessor);
                            solvers.add(solver);
                            solver.fork();
                        }
                    }
                    // Join all the solvers
                    for (ForkJoinSolver solver: solvers)
                    {
                        List<Integer> path = solver.join();
                        if (path != null) {
                            // If there is a result, can also be null for all solvers.
                            result = path;
                        }
                    }
                    if (result != null) 
                    {
                        return result;
                    }
                }
                // Reaching this block means that there is only 
                // one unvisited neighbor next to current tile
                else
                {
                    for (int nb: maze.neighbors(current)) 
                    {
                        frontier.push(nb);
                        if (!visited.contains(nb))
                            predecessor.put(nb, current);
                    }
                }
            }
        }
        
        return null;
    }

    private int getUnvisitedNeighbors(int current, Set<Integer> visited)
    {
        int unvisited = 0;
        Set<Integer> neighbors = maze.neighbors(current);
        
        for (int nb: neighbors)
        {
            if (!visited.contains(nb))
            {
                unvisited += 1;
            }
        }
        return unvisited;
    }

    @Override
    protected List<Integer> pathFromTo(int from, int to) {
        List<Integer> path = new LinkedList<>();
        Integer current = to;
        while (current != from) {
            path.add(current);
            current = predecessor.get(current);
            if (current == null)
                return null;
        }
        path.add(from);
        Collections.reverse(path);
        return path;
    }

}
