/**
  * Created by Mona on 03/02/2018.
  */
import java.util.concurrent.CountDownLatch

import org.apache.zookeeper.Watcher.Event
import org.apache.zookeeper.{WatchedEvent, Watcher, ZooKeeper};

object ZookeeperClient {

  private val CONNECTION_STRING = "127.0.0.1:2181";
  private val SESSION_TIMEOUT = 5000;
  private val latch = new CountDownLatch(1);
  def init(): Unit ={
    val zk:ZooKeeper = new ZooKeeper(CONNECTION_STRING,SESSION_TIMEOUT,new Watcher {
      override def process(event: WatchedEvent): Unit = {
        if (event.getState() == Event.KeeperState.SyncConnected){
          latch.countDown();
        }
      }
    });
    latch.await();
  }
}
