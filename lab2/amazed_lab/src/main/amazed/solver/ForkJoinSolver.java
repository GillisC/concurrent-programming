package amazed.solver;

import amazed.maze.Maze;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
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

    static AtomicBoolean heartFound = new AtomicBoolean(false);

    public ForkJoinSolver(Maze maze, int start, Set<Integer> visited, Map<Integer, Integer> predecessor)
    {
        this(maze);
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
                System.out.println("stopping...");
                return null;
            }
            

            // If we reach the heart
            if (maze.hasGoal(current)) 
            {
                heartFound.set(true);
                System.out.println("found it");
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
                    List<ForkJoinSolver> solvers = new ArrayList<>();
                    List<Integer> result = null;

                    for (int nb: neighbors) 
                    {   
                        if (!visited.contains(nb))
                        {
                            predecessor.put(nb, current);
                            ForkJoinSolver solver = new ForkJoinSolver(this.maze, nb, this.visited, this.predecessor);
                            solvers.add(solver);
                            solver.fork();
                        }
                    }
                    for (ForkJoinSolver solver: solvers)
                    {
                        List<Integer> path = solver.join();
                        if (path != null) {
                            result = path;
                        }
                    }
                    if (result != null) 
                    {
                        return result;
                    }
                }
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
}
