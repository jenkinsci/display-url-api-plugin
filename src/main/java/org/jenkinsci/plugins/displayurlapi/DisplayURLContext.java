package org.jenkinsci.plugins.displayurlapi;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.PluginManager;
import hudson.PluginWrapper;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.Run;
import java.io.Closeable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import jenkins.model.Jenkins;

/**
 * Holds contextual information that can be used when generating an URL for display. On the current thread, each context
 * is layered with the most specific context information being provided when queried.
 */
public class DisplayURLContext implements Closeable {

    /**
     * The name of this plugin.
     */
    private static final String OUR_PLUGIN_NAME = "display-url-api";
    /**
     * The current thread's context.
     */
    private static ThreadLocal<Stack<DisplayURLContext>> context = new ThreadLocal<>();

    private static final Cache<String, Optional<PluginWrapper>> CACHE =
        CacheBuilder.newBuilder()
            .maximumSize(Integer.getInteger( DisplayURLContext.class.getName() + ".cache.size", 1000))
            // cache ttl default 5 minutes
            .expireAfterAccess( Integer.getInteger( DisplayURLContext.class.getName() + ".cache.ttl", 300000), TimeUnit.MILLISECONDS)
            //.weakValues() not sure we need this as entries are pluginWrapper will never be garbaged
            .build();

    /**
     * Class names that we expect to be in the stack trace for calls to {@link #open()}.
     */
    private static Set<String> ourPluginClassNames = new HashSet<>(Arrays.asList(
            DisplayURLContext.class.getName(),
            DisplayURLProvider.class.getName(),
            DisplayURLProvider.DisplayURLProviderImpl.class.getName()
    ));

    private final DisplayURLContext parent;
    /**
     * The {@link Queue.Task}.
     */
    @CheckForNull
    private Queue.Task task;

    /**
     * The {@link Queue.Executable}.
     */
    @CheckForNull
    private Queue.Executable executable;

    /**
     * The {@link Job}.
     */
    @CheckForNull
    private Job<?, ?> job;

    /**
     * The {@link Run}.
     */
    @CheckForNull
    private Run<?, ?> run;

    /**
     * The {@link PluginWrapper}.
     */
    @CheckForNull
    private PluginWrapper plugin;

    /**
     * And additional custom attributes.
     */
    @CheckForNull
    private Map<String, String> attributes;

    /**
     * Constructor.
     */
    private DisplayURLContext(DisplayURLContext parent) {
        this.parent = parent;
    }

    /**
     * Try and infer the plugin by looking in the calling stack trace for the first plugin that is not this one.
     */
    private void guessPlugin() {
        StackTraceElement[] stack = (new Throwable()).getStackTrace();
        PluginManager manager = Jenkins.getInstance().getPluginManager();
        ClassLoader loader = manager.uberClassLoader;
        for (StackTraceElement frame : stack) {
            String cname = frame.getClassName();
            if (ourPluginClassNames.contains(cname)) {
                continue;
            }
            Optional<PluginWrapper> wrapper = CACHE.getIfPresent( cname);
            if(wrapper != null){
                if(wrapper.isPresent()){
                    plugin = wrapper.get();
                }
            } else
            {
                try {
                    Class<?> clazz = loader.loadClass( cname );
                    PluginWrapper pluginWrapper = manager.whichPlugin( clazz );
                    if ( pluginWrapper != null && !OUR_PLUGIN_NAME.equals( pluginWrapper.getShortName() ) ) {
                        plugin = pluginWrapper;
                        CACHE.put( cname, Optional.of( pluginWrapper ) );
                        break;
                    } else {
                        CACHE.put( cname, Optional.empty() );
                    }
                } catch ( ClassNotFoundException e ) {
                    // ignore, it's not a plugin
                }
            }
        }
    }


    /**
     * Opens a {@link DisplayURLContext} for the current thread.
     * @param guessPlugin try to infer the current plugin (resource intensive as requires a stack trace and class loading). Use {@code false} if you know the caller will always be Jenkins core.
     * @return the {@link DisplayURLContext}.
     */
    @NonNull
    public static DisplayURLContext open(boolean guessPlugin) {
        Stack<DisplayURLContext> stack = DisplayURLContext.context.get();
        if (stack == null) {
            stack = new Stack<>();
            DisplayURLContext.context.set(stack);
        }
        DisplayURLContext context = new DisplayURLContext(stack.isEmpty() ? null : stack.peek());
        if (stack.isEmpty() && guessPlugin) {
            context.guessPlugin();
        }
        stack.push(context);
        return context;
    }

    /**
     * Opens a {@link DisplayURLContext} for the current thread.
     *
     * @return the {@link DisplayURLContext}.
     */
    @NonNull
    public static DisplayURLContext open() {
        return open(true);
    }

    /**
     * Uses the supplied {@link Queue.Task} to fill in as much of the context as possible. If the task implements {@link
     * Job} then the {@link #job()} will also be set. Prefer calling {@link #job(Job)} if you know the task is a {@link
     * Job} already.
     *
     * @param task the task.
     * @return {@code this} for method chaining.
     */
    @NonNull
    public DisplayURLContext task(@CheckForNull Queue.Task task) {
        this.task = task;
        if (task instanceof Job) {
            this.job = (Job<?, ?>) task;
        }
        return this;
    }

    /**
     * Uses the supplied {@link Queue.Executable} to fill in as much of the context as possible. The owning task of the
     * executable will be used to set the {@link #task()}. If the task implements {@link Run} then the {@link #run()}
     * and {@link #job()} will also be set. Prefer calling {@link #run(Run)} if you know the executable is a {@link Run}
     * already.
     *
     * @param executable the executable.
     * @return {@code this} for method chaining.
     */
    @NonNull
    public DisplayURLContext executable(@CheckForNull Queue.Executable executable) {
        this.executable = executable;
        if (executable != null) {
            this.task = executable.getParent().getOwnerTask();
        }
        if (executable instanceof Run) {
            this.run = (Run<?, ?>) executable;
            this.job = ((Run<?, ?>) executable).getParent();
        }
        return this;
    }

    /**
     * Uses the supplied {@link Job} to fill in as much of the context as possible. If the job is also a {@link
     * Queue.Task} then {@link #task()} will also be set.
     *
     * @param job the job.
     * @return {@code this} for method chaining.
     */
    @NonNull
    public DisplayURLContext job(@CheckForNull Job<?, ?> job) {
        this.job = job;
        if (job instanceof Queue.Task) {
            this.task = (Queue.Task) job;
        }
        return this;
    }

    /**
     * Uses the supplied {@link Run} to fill in as much of the context as possible. The {@link Run#getParent()} will be
     * set to the {@link #job()} and {@link #task()} as appropriate. If the run is also a {@link Queue.Executable} then
     * {@link #executable()} will also be set.
     *
     * @param run the run.
     * @return {@code this} for method chaining.
     */
    @NonNull
    public DisplayURLContext run(@CheckForNull Run<?, ?> run) {
        this.run = run;
        if (run != null) {
            if (run instanceof Queue.Executable) {
                this.executable = (Queue.Executable) run;
            }
            job(run.getParent());
        }
        return this;
    }

    /**
     * Overrides the inferred plugin tha is requesting the URL to be generated with the supplied plugin.
     *
     * @param plugin the plugin.
     * @return {@code this} for method chaining.
     */
    @NonNull
    public DisplayURLContext plugin(@CheckForNull PluginWrapper plugin) {
        this.plugin = plugin;
        return this;
    }

    /**
     * Adds custom attributes to the context.
     *
     * @param name the name of the attribute.
     * @param value the value of the attribute (may be {@code null}).
     * @return {@code this} for method chaining.
     */
    @NonNull
    public DisplayURLContext attribute(String name, String value) {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        }
        this.attributes.put(name, value);
        return this;
    }

    /**
     * Gets the {@link Queue.Task} associated with the current URL generation (if any).
     *
     * @return the {@link Queue.Task} associated with the current URL generation or {@code null} if none.
     */
    @CheckForNull
    public Queue.Task task() {
        if (task == null && parent != null) {
            return parent.task();
        }
        return task;
    }

    /**
     * Gets the {@link Queue.Executable} associated with the current URL generation (if any).
     *
     * @return the {@link Queue.Executable} associated with the current URL generation or {@code null} if none.
     */
    @CheckForNull
    public Queue.Executable executable() {
        if (executable == null && parent != null) {
            return parent.executable();
        }
        return executable;
    }

    /**
     * Gets the {@link Job} associated with the current URL generation (if any).
     *
     * @return the {@link Job} associated with the current URL generation or {@code null} if none.
     */
    @CheckForNull
    public Job<?, ?> job() {
        if (job == null && parent != null) {
            return parent.job();
        }
        return job;
    }

    /**
     * Gets the {@link Run} associated with the current URL generation (if any).
     *
     * @return the {@link Run} associated with the current URL generation or {@code null} if none.
     */
    @CheckForNull
    public Run<?, ?> run() {
        if (run == null && parent != null) {
            return parent.run();
        }
        return run;
    }

    /**
     * Gets the best guess as to the {@link PluginWrapper} requesting the current URL generation (if any).
     *
     * @return the {@link PluginWrapper} most closely associated with the current URL generation or {@code null} if
     *         initiated by Jenkins core.
     */
    @CheckForNull
    public PluginWrapper plugin() {
        if (plugin == null && parent != null) {
            return parent.plugin();
        }
        return plugin;
    }

    /**
     * Gets the custom attribute value for the current URL generation..
     *
     * @param name the name of the attribute.
     * @return the value or {@code null}.
     */
    @CheckForNull
    public String attribute(String name) {
        if ((attributes == null || !attributes.containsKey(name)) && parent != null) {
            return parent.attribute(name);
        }
        return attributes == null ? null : attributes.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        Stack<DisplayURLContext> stack = context.get();
        if (stack != null) {
            stack.pop();
            if (stack.isEmpty()) {
                context.remove();
            }
        }
    }
}
