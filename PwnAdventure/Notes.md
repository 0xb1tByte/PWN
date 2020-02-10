## === Intro ===
The below notes & commands are based on what has been explained on [Pwn Adventure 3: Pwnie Island](https://www.youtube.com/playlist?list=PLhixgUqwRTjzzBeFSHXrw9DnQtssdAwgG) videos

## === Info Gathering ===

## 1 - File type 
**Command :** <br>
file <fileName>

## 2 - Printing The Shared Object (Dynamic Libraries)
**Command :** <br>
ldd <fileName>

**NOTE:** <br>
- the must important file here is : **libGameLogic.so** , if we check it with file command , we get : <br>
```libGameLogic.so: ELF 64-bit LSB shared object, x86-64, version 1 (SYSV), dynamically linked, not stripped
- the libGameLogic is NOT STRIPPED
```

## 3 - Checking the game process
**Command :** <br>
pstree -aslp 
**Output :** <br>
``` 
  │   │   │   │   │   └─PwnAdventure3-L,26238
  │   │   │   │   │       ├─{AsyncIOSystem},26247
  │   │   │   │   │       ├─{FMessag-.Router},26248
  │   │   │   │   │       ├─{PoolThread 0},26239
  │   │   │   │   │       ├─{PoolThread 1},26240
  │   │   │   │   │       ├─{PoolThread 2},26241
  │   │   │   │   │       ├─{PwnAdventure3-L},26253
  │   │   │   │   │       ├─{RTHeartBeat 1},26260
  │   │   │   │   │       ├─{RenderThread 1},26259
  │   │   │   │   │       ├─{SDLTimer},26245
  │   │   │   │   │       ├─{TaskGraph 0},26242
  │   │   │   │   │       ├─{TaskGraph 1},26243
  │   │   │   │   │       ├─{TaskGraph 2},26244
  │   │   │   │   │       └─{threaded-ml},26252
  ```

## 4 - Discovering the /proc folder for the game

### 4.1 - cd to the game proc folder
**Command :** <br>
cd /proc/<pid>

### 4.2 - listing the files
**Command :** <br> 
ls

**Output :** <br>
```  
attr        coredump_filter  gid_map    mountinfo   oom_score      schedstat  status
autogroup   cpuset           io         mounts      oom_score_adj  sessionid  syscall
auxv        cwd              limits     mountstats  pagemap        setgroups  task
cgroup      environ          loginuid   net         personality    smaps      timers
clear_refs  exe              map_files  ns          projid_map     stack      uid_map
cmdline     fd               maps       numa_maps   root           stat       wchan
comm        fdinfo           mem        oom_adj     sched          statm
```  


### 4.3 - Reading the memory map for the game 
**Command :** <br> 
cat maps | less

**Output :** <br>
the output is huge, but the important stuff here might be :
```
			- the virtual address for the game
			- Stack address
			- the .so files addresses
```

### 4.4 - Reading the environment variables for the game 
**Command :** <br> 
cat environ | sed 's/\x00/\x0a/g' 

**NOTE:** 
- /proc : This special directory holds all the details about Linux system, including its kernel, processes, and configuration parameters
- I think the most interesting stuff here these files: 
```
	- environ : this file contains environment variables
	- maps : this file output the memory map for the game, we can get the stack address from here
	- fd : this folder contains a list of all the currently opened file descriptors and to which file
          which means it's list all the files that's currently accessed by the game
```

## 5 - Analyzing Game Network 
### 5.1 Showing both listening & non-listening Sockets 
**Command :** <br> 
netstat -a -c | grep -i pwn 


### 5.2 Using Wireshark
we can use wireshark here also and monitor the traffic




