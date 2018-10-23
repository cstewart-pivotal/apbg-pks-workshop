package io.pivotal.pks.demo.fortunebackend;

import com.alibaba.fastjson.JSON;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.*;

@Path("fortune")
public class FortuneResource {

    private static Logger LOG = LoggerFactory.getLogger(FortuneResource.class);

    final JedisPoolConfig poolConfig = buildPoolConfig();
    JedisPool jedisPool = new JedisPool(poolConfig, getRedisHost());

    private static String getRedisHost() {
        String envHost = System.getenv("REDIS_HOST");
        LOG.info("REDIS_HOST: " + envHost);
        return (envHost == null) ? "localhost" : envHost;
    }

    private JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }

    @GET
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFortunes() {
        LOG.info("Getting all fortunes from Redis");
        List<Fortune> fortunes = new ArrayList<>();

        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> keySet = jedis.keys("*");
            keySet.forEach((key)-> {
                fortunes.add(JSON.parseObject(jedis.get(key.toString()), Fortune.class));
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        return Response
                .ok(new GenericEntity<List<Fortune>>(fortunes){})
                .build();
    }

    @GET
    @Path("random")
    @Produces(MediaType.APPLICATION_JSON)
    public Fortune getRandfortune() {
        LOG.info("Getting random fortune from Redis");
        List<Fortune> all = (List<Fortune>) getFortunes().getEntity();
        if(all.isEmpty()) return null;

        Fortune f = all.get(new Random().nextInt(all.size()));
        LOG.debug("Fortune-[" + f + "]");
        return f;
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Fortune getFortune(@PathParam("id") Long id) {
        LOG.info("Getting fortune for key [" + id + "] from Redis");
        try (Jedis jedis = jedisPool.getResource()) {
            String result = jedis.get(id.toString());
            return JSON.parseObject(jedis.get(id.toString()), Fortune.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Fortune save(Fortune input) {
        LOG.info("Saving fortune [" + input + "] into Redis");
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> keySet = jedis.keys("*");
            input.setId(new Long(keySet.size() + 1));
            jedis.set(input.getId().toString(), JSON.toJSONString(input));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        return input;
    }


    @DELETE
    @Path("clear")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Fortune> clear() {
        LOG.info("Clearing all fortunes from Redis");
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.flushAll();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        LOG.info("Deleted");
        return Collections.EMPTY_LIST;
    }

    @Path("kill")
    @Getter
    public void kill() {
        System.exit(-1);
    }
}
