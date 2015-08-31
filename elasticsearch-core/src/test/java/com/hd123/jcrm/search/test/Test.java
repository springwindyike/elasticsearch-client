package com.hd123.jcrm.search.test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.RateLimiter;
import com.hd123.jcrm.search.test.model.TestData;

/**
 * @author liyue
 */
@SuppressWarnings("unused")
public class Test {

  private static <T> void test(List<T> lists) {

  }

  public static void main(String[] args) {

    List<TestData> datas = Lists.newArrayList();
    test(datas);

    String n = null;
    // Optional<String> s = Optional.of(n);
    String r = "fsfs  ";
    String d = "Gd gdg ";
    Joiner jon = Joiner.on(",").skipNulls();
    String f = "  df    fdfd    4fdFDF   fdfdf ";
    String w = "dsdfsf,fsf  , fsf ,fsfs   ,fsfs";
    Multimap<String, String> m = ArrayListMultimap.create();
    m.put("2", "dsd");
    m.put("2", "ewew");
    // new MapMaker().concurrencyLevel(5).makeMap();

    System.out.println(new String(r.getBytes(Charsets.UTF_8)));
    System.out.println(jon.join(null, r, null, d));
    System.out.println(Splitter.onPattern("\\s+").trimResults().omitEmptyStrings().splitToList(f));
    System.out.println(Splitter.on(",").trimResults().omitEmptyStrings().splitToList(w));
    System.out.println(CharMatcher.WHITESPACE.trimAndCollapseFrom(w, '3'));
    System.out.println(CharMatcher.JAVA_DIGIT.or(CharMatcher.JAVA_UPPER_CASE).retainFrom(f));

    final ListeningExecutorService lisExecService = MoreExecutors
        .listeningDecorator(Executors.newCachedThreadPool());
    final RateLimiter rlimiter = RateLimiter.create(10); // Semaphore
    rlimiter.acquire();
    final ListenableFuture<String> lisFuture = lisExecService.submit(new Callable<String>() {

      @Override
      public String call() throws Exception {
        System.out.println("test start");
        Thread.sleep(1000);
        return "test";
      }
    });

    Futures.addCallback(lisFuture, new FutureCallback<String>() {

      @Override
      public void onSuccess(String result) {
        System.out.println(String.format("%s done", result));
      }

      @Override
      public void onFailure(Throwable t) {
        System.out.println("fail");
      }
    });

    System.out.println("test continue");
  }

  // guava 缓存
  // .expireAfterAccess(5L, TimeUnit.MINUTES) //5分钟后缓存失效
  // .softValues() //使用SoftReference对象封装value, 使得内存不足时，自动回收
  // .removalListener(new TradeAccountRemovalListener()) //注册缓存对象移除监听器
  // .ticker(Ticker.systemTicker()) //定义缓存对象失效的时间精度为纳秒级
  // .concurrencyLevel(10) //允许同时最多10个线程并发修改
  // .refreshAfterWrite(5L, TimeUnit.SECONDS) //5秒中后自动刷新
  // .ticker(Ticker.systemTicker()) //定义缓存对象失效的时间精度为纳秒级
  final LoadingCache<String, TestData> cache = CacheBuilder.newBuilder().concurrencyLevel(2)
      .expireAfterWrite(2, TimeUnit.SECONDS).recordStats().softValues()
      .refreshAfterWrite(1, TimeUnit.SECONDS)
      .removalListener(new RemovalListener<String, TestData>() {

        @Override
        public void onRemoval(RemovalNotification<String, TestData> notification) {
          // TODO Auto-generated method stub

        }
      }).build(new CacheLoader<String, TestData>() {

        @Override
        public TestData load(String key) throws Exception {
          // TODO Auto-generated method stub
          return null;
        }
      });

  private void load() {
    // cache.put(key, value);
  }

}
