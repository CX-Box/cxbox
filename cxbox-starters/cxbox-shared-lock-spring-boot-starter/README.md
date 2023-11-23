# Cxbox starter for shared lock

## Description of the algorithm operation

The xbox-shared-lock-spring-boom-starter starter is used to install locks when loading metadata, in the absence of a starter, the lock is not set.
When the starter is running, the standard lock setting algorithm from the StandardCxboxSharedLock class is used, to use a custom algorithm, you need to implement the interface
CxboxSharedLock and override the void acquireAndExecute(Runnable runnable) methods - to implement the lock algorithm, void waitForLock() - to implement the waiting algorithm when the lock is set.

## Prerequisites
Your project uses cxbox with cxbox-starter-parent, e.g. you have in your pom.xml:
```
<parent>
    <groupId>org.cxbox</groupId>
    <artifactId>cxbox-starter-parent</artifactId>
    <version>CHANGE_ME</version>
</parent>
```

## Getting started
### Dependency
In your pom.xml add
```
<dependency>
    <groupId>org.cxbox</groupId>
    <artifactId>cxbox-shared-lock-spring-boot-starter</artifactId>
</dependency>
```

### (Optional) Shared lock setting
You can set optional parameters in your application.yaml

```
cxbox:
  shared-lock:
    timeout :    - timer until the lock is released in milliseconds (default: 1_800_000 ms)
    check-interval: - the interval between the lock check in milliseconds (default: 1000 ms)
```

## Algorithms

### The algorithm of acquireAndExecute(Runnable runnable)

acquireAndExecute(Runnable runnable) calls create LockRowIfNotExist of the metaLockService.createLockRowIfNotExist() service, 
which checks for the presence of a log row in the LOCK_BASE table, and creates it in the absence, then checking for lock by the metaLockService.isLock() method, if there is a lock, the waitForLock() method is launched, 
waiting for the lock to end or the lock timeout. Then the metaLockService.updateLock(LocalStatusType.LOCK) method works, changing the status to LOCK and updating the lock_date status to the current one, if the update fails, the waitForLock() method is launched.
Next, the logic passed to Runnable is called, after loading the metadata, metaLockService.updateLock(LockStatusType.UNLOCK) is called, the transfer to the UNLOCK status.

### The algorithm of waitUnLock()

waitUnLock() enters the while loop while Lock is standing, to exit the loop, it checks with an interval of 1 second or specified in cxbox.meta.check-lock-interval checks whether the status
has changed to UNLOCK or whether the default timeout of 30 minutes has ended or the value specified in cxbox.meta.base-lock-timer, adding timeout values to the LocalDateTime received from Lock_date and checking whether the result is up to the current time.


  


