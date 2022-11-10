# Raft-Java
Study base on MIT 6.824 raft course. I implement it according to my own ideas, so some things may be different from raft standard.
<br/><br/><br/>
 [![Raft](https://user-images.githubusercontent.com/83362909/201148733-a9b4ebcf-5b15-4a70-a778-37f298ccf28f.jpg)](http://thesecretlivesofdata.com/raft/#home) 
 <br/>
 [![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
  <img src="https://img.shields.io/badge/platform-java-9cf" />
 <img src="https://img.shields.io/badge/version-1.8-yellowgreen"/>
 <br/>
<img src="https://img.shields.io/github/last-commit/Ritusan/color-library" alt="last-commit" />
<img src="https://img.shields.io/badge/author-Daydreamer--ia-orange" alt="languages-top" /><br/><br/>
Here are my notes in Chinese: 
## 概念

> 强一致模型

所谓的强一致性并**不是指集群中所有节点在任一时刻的状态必须完全一致**，而是指一个目标，即**让一个分布式系统看起来只有一个数据副本，并且读写操作都是原子的**，这样应用层就可以忽略系统底层多个数据副本间的同步问题。将一个强一致性分布式系统当成一个整体，一旦某个客户端成功的执行了写操作，那么所有客户端都一定能读出刚刚写入的值。即使**发生网络分区故障，或者少部分节点发生异常，整个集群依然能够像单机一样提供服务**。

> 共识算法

共识算法 `Consensus Algorithm` 就是用来做这个事情的，它保证**即使在小部分节点故障的情况**下，**系统仍然能正常对外提供服务**。共识算法通常基于状态复制机 `Replicated State Machine` 模型，也就是**所有节点从同一个 `state` 出发，经过同样的操作 `log`，最终达到一致的 `state`**。

共识算法是构建强一致性分布式系统的基石，`Paxos` 是共识算法的代表，而 **`Raft` 则是其作者在博士期间研究 `Paxos` 时提出的一个 `Multi Paxos` 变种**。

**`Raft` 使用 `Quorum` 机制 (实现数据冗余和最终一致性的投票算法) 来实现共识和容错**，我们将对 `Raft` 集群的操作称为提案，每当发起一个提案，必须得到大多数节点的同意才能提交。

> 核心算法

`Raft` 核心算法其实就是由这三个子问题组成的：**选主（`Leader election`）**、**日志复制（`Log replication`）**、**安全性（`Safety`）**。这三部分**共同实现了 `Raft `核心的共识和容错机制**。

> 实际落地面临的问题

- 第一个是关于**日志无限增长**的问题。**`Raft` 将操作包装成为了日志，集群每个节点都维护了一个不断增长的日志序列，状态机只有通过重放日志序列来得到**。但由于这个日志序列可能会随着时间流逝不断增长，因此必须有一些办法来**避免无休止的磁盘占用和过久的日志重放**，这一部分叫**日志压缩**（**`Log compaction`**）。		
- 第二个是关于**集群成员变更**的问题。一个` Raft` 集群不太可能永远是固定几个节点，总有**扩缩容**的需求，或是**节点宕机需要替换**的时候。`Raft` 给出了一种安全变更集群成员的方式，这一部分叫**集群成员变更**（**·Cluster membership change·**）。

## 选主

> 什么是选主？

选主（`Leader election`）就是在分布式系统内**抉择出一个主节点来负责一些特定的工作**。在执行了选主过程后，**集群中每个节点都会识别出一个特定的、唯一的节点作为 `leader`**。

> `Raft` 为何需要选主？

当有一系列的决策需要被制定的时候，先选出一个 `leader` 节点然后让它去协调所有的决策，这样算法会更加简单快速，而且**由于只有一个 `leader` 作为提案节点，那么就不需要 `Prepare` 阶段进行抢锁，也避免活锁问题**。

原生的 `Paxos` 算法使用了一种点对点（`peer-to-peer`）的方式，**所有节点地位是平等的**，如果没有 `leader` ，所有节点都可以是提案节点和决策节点，那么**协商请求可以来自各个节点，提高了日志复制的逻辑复杂度，使算法变得更加复杂**。

### 选主过程

> 节点角色

- **Leader**: 领导人，所有请求的处理者，接收客户端发起的操作请求，**写入本地日志后同步至集群其它节点**；
- **Follower**: 追随者，请求的被动更新者，**从 `leader` 接收更新请求，写入本地文件**。如果**客户端的操作请求发送给了 `follower`，会首先由 `follower` 重定向给 `leader`**；
- **Candidate**: 候选人，如果 **`follower` 在一定时间内没有收到 `leader` 的心跳**，则**判断 `leader` 可能已经故障，此时启动 `leader election` 过程，本节点切换为 `candidate` 直到选主结束**；
  - 所有的候选人都可能升级为 `Leader` ，否则就降级为 `Follower`；

### 任期

每开始一次新的选举，称为一个**任期**（**`term`**），每个 `term` 都有一个**严格递增**的整数与之关联。

- **每当 `candidate` 触发 `leader election` 时都会增加 `term`，如果一个 `candidate` 赢得选举，他将在本 `term` 中担任 `leader` 的角色**；
- 但并**不是每个 ` term` 都一定对应一个 `leader`，有时候某个`term` 内会由于选举超时导致选不出 `leader`，这时 `candicate` 会递增 `term` 号并开始新一轮选举**；

`Term` 更像是一个**逻辑时钟**（**`logic clock`**）的作用，有了它，就可以发现哪些节点的状态已经过期。每一个节点都保存一个 `current term`，**在通信时带上这个 `term` 号**。

- 所有的节点都有一个任期，**在集群内正常情况下都是保持同步的**。
- 在**同一个任期**内，作为**候选人可以发起一次投票请求，**作为**追随者只能为一个节点投一次票**，作为**领导者需要负责维护心跳和日志复制**；

> 请求

节点间**通过 `RPC` 来通信**，主要有两类 `RPC` 请求：

- **`RequestVote RPCs`**: 用于 **`candidate` 拉票选举**；
- **`AppendEntries RPCs`**: 用于 **`leader` 向其它节点复制日志以及同步心跳**；

### 节点状态转换

> 以下为图示

![6f7a7b1d77dc2da1292f2ad090d1dc9](https://user-images.githubusercontent.com/83362909/200126219-689e49d6-a832-4a58-a729-9f09782184a0.jpg)


- `Raft` 的选主基于一种心跳机制，**集群中每个节点刚启动时都是 `follower` 身份（Step: starts up），`leader` 会周期性的向所有节点发送心跳包来维持自己的权威**。
  - 如果所有节点**在同一时刻启动**，经过**同样的超时时间**后同时发起选举，整个集群会变得低效不堪，极端情况下甚至会一直选不出一个主节点；
  - **`Raft` 巧妙的使用了一个随机化的定时器，让每个节点的“超时时间”在一定范围内随机生成，这样就大大的降低了多个节点同时发起选举的可能性**；
- 如果**一个 `follower` 在一段时间内没有收到任何心跳，也就是选举超时，那么它就会主观认为系统中没有可用的 `leader`，并发起新的选举**（**Step: times out, starts election**）。
- `Follower` 切换为 `candidate` 并向集群其他节点发送“请给自己投票”的消息后，接下来会有三种可能的结果：
  - 选举成功 (**Step: receives votes from majority of servers**)：**当 `candicate` 从整个集群的大多数节点获得了针对同一 `term` 的选票时，它就赢得了这次选举**，立刻将自己的**身份转变为 `leader` 并开始向其它节点发送心跳来维持自己的权威**；
  - 选举失败 (**Step: discovers current leader or new term**)：`candidate` 在等待投票回复的时候，可能会突然收到其它自称是 **`leader` 的节点发送的心跳包**，如果这个心跳包里携带的 `term` **不小于** `candidate` 当前的 `term`，那么 **`candidate` 会承认这个 `leader`，并将身份切回 `follower`==**。这说明其它节点已经成功赢得了选举，只需立刻跟随即可。但**如果心跳包中的 `term` 比自己小，`candidate` 会拒绝这次请求并保持选举状态**；
  - 选举超时 (**Step: times out, new election**)：如果**有多个 `follower` 同时成为` candidate`，选票是可能被瓜分的**，如果**没有任何一个 `candidate` 能得到大多数节点的支持，那么每一个 `candidate` 都会超时**。此时 **`candidate` 需要增加自己的 `term`，然后发起新一轮选举**。如果这里不做一些特殊处理，选票可能会一直被瓜分，导致选不出 `leader` 来。
    - 所谓特殊处理就是**指定的超时时间，每个节点都是随机的**；
    - **每个作为 `Follower` 的节点一个任期内只能为一个  `candidate` 投票**；
- 节点降级 (**discovers server with higher term**)：当 **`leader `节点发生了宕机或网络断连**，此时其它 `follower` 会收不到 `leader` 心跳，首个触发超时的节点会变为 `candidate` 并开始拉票。由于该 `candidate` 的 `term` 大于原 `leader` 的 `term`，因此所有 `follower` 都会投票给它，**这名 `candidate` 会变为新的 `leader`**。一段时间后**原 `leader` 恢复了，收到了来自新 `leader` 的心跳包，发现心跳中的 `term` 大于自己的 `term`，此时该节点会立刻切换为 `follower` 并跟随的新 `leader`**；

## 日志复制

> 什么是日志复制

共识算法通常基于**状态复制机**（**Replicated State Machine**）模型，所有节点从**同一个 `state`** 出发，经过一系列**同样操作 `log`** 的步骤，最终也必将达到**一致的 `state`**。也就是说，只要我们保证集群中所有节点的 `log` 一致，那么经过一系列应用后**最终得到的状态机也就是一致的**。

`Raft` 赋予了 `leader` 节点更强的领导力（**`Strong Leader`**）。那么 `Raft` 保证 `log` 一致的方式就很容易理解了，即**所有 `log` 都必须交给 `leader` 节点处理，并由 `leader` 节点复制给其它节点**。

### 机制解析

一旦` leader `被票选出来，它就承担起**领导整个集群的责任**了，开始**接收客户端请求，并将操作包装成日志，并复制到其它节点上去**。

> 复制的大致流程

- `Leader` 为客户端提供服务，**客户端的每个请求都包含一条即将被状态复制机执行的指令**；
- `Leader` 把**该指令作为一条新的日志附加到自身的日志集合**，然后向其它节点发起附加条目请求（**AppendEntries RPC**），来**要求它们将这条日志附加到各自本地的日志集合**；
- 当这条日志**已经确保被安全的复制**，即**大多数节点都已经复制后，`leader` 会将该日志应用到它本地的状态机中，然后把操作成功的结果返回给客户端**；

> 日志模型

![b4592705729eb7acd02e6e069d8c047](https://user-images.githubusercontent.com/83362909/200125245-ed3f5e2d-2c68-4fff-a0eb-8c21765a6d3a.jpg)

每条日志除了存储状态机的操作指令外，还会拥有一个**唯一的整数索引值**（**`log index`**）来表明它在日志集合中的位置。此外，每条日志还会存储一个 **term** 号（日志条目方块最上方的数字，相同颜色 `term` 号相同），该 `term` 表示 `leader `收到这条指令时的当前任期，`term` 相同的 `log` 是由同一个 `leader` 在其任期内发送的。

当一条日志被 `leader` 节点认为可以安全的应用到状态机时，即 **当 `leader` 得知这条日志被集群过半的节点复制成功时**， 称这条日志是 **`committed`**。

`Raft` 保证所有 `committed` 日志都已经被**持久化**，且“**最终**”一定会被状态机应用。

所以，`Raft` 保证的只是集群内日志的一致性，而我们真正期望的集群对外的状态机一致性需要我们做一些额外工作。

### 一致性保证

> `Raft` 传输的心跳和日志为何需要带上 `term` ？

` term` 可以用来检查不同节点间日志是否存在不一致的情况。

`Raft` 保证：**如果不同的节点日志集合中的两个日志条目拥有相同的 `term` 和 `index`，那么它们前面一定存储了相同的日志集合==**。因为 `Raft` 要求 **`leader` 在一个 `term` 内针对同一个 `index` 只能创建一条日志**，并且**永远不会修改它**。

这是因为 `leader` 发出的**日志复制请求 (`AppendEntries RPC`)** 中会额外携带**上一条**日志的 ` (term, index)`，如果 `follower` 在本地**找不到**相同的 `(term, index)` 日志，则**拒绝接收这次新的日志**。

> 可能出现的日志不一致场景

在所有节点正常工作的时候，`leader` 和 `follower` 的日志总是保持一致，`AppendEntries RPC` 也永远不会失败。

![817146fafab3331ec74e2b7e2bb4e0d](https://user-images.githubusercontent.com/83362909/200126245-505ec455-8b06-403d-b368-b13ce662778e.jpg)


- **Follower 日志落后于 leader**：**follower 宕机了一段时间**，follower-a 从收到 (term6, index9) 后开始宕机，follower-b 从收到 (term4, index4) 后开始宕机。
- **Follower 日志比 leader 多 term6**：当 term6 的 leader 正在将 (term6, index11) 向 follower 同步时，该 leader 发生了宕机，且此时只有 follower-c 收到了这条日志的 AppendEntries RPC。然后经过一系列的选举，term7 可能是选举超时，也可能是 leader 刚上任就宕机了，最终 term8 的 leader 上任了，成就了我们看到的场景 c。
- **Follower 日志比 leader 多 term7**：当 term6 的 leader 将 (term6, index10) 成功 commit 后，发生了宕机。此时 term7 的 leader 走马上任，连续同步了两条日志给 follower，然而还没来得及 commit 就宕机了，随后集群选出了 term8 的 leader。
- **Follower 日志比 leader 少 term5 ~ 6，多 term4**：当 term4 的 leader 将 (term4, index7) 同步给 follower，且将 (term4, index5) 及之前的日志成功 commit 后，发生了宕机，紧接着 follower-e 也发生了宕机。这样在 term5~7 内发生的日志同步全都被 follower-e 错过了。当 follower-e 恢复后，term8 的 leader 也刚好上任了。
- **Follower 日志比 leader 少 term4 ~ 6，多 term2 ~ 3**：当 term2 的 leader 同步了一些日志（index4 ~ 6）给 follower 后，尚未来得及 commit 时发生了宕机，但它很快恢复过来了，又被选为了 term3 的 leader，它继续同步了一些日志（index7~11）给 follower，但同样未来得及 commit 就又发生了宕机，紧接着 follower-f 也发生了宕机，当 follower-f 醒来时，集群已经前进到 term8 了。

### 如何处理日志不一致

> 真实世界的集群情况很复杂，那么 Raft 是如何应对这么多不一致场景的呢？

`Raft` **强 `Leader` 模型**：**强制要求 `follower` 必须复制 `leader` 的日志集合来解决不一致问题**。

即 **`follower` 节点上任何与 `leader` 不一致的日志，都会被 `leader` 节点上的日志所覆盖**。这并不会产生什么问题，因为某些选举上的限制，**如果 `follower` 上的日志与 leader 不一致，那么该日志在 `follower` 上一定是未提交的**。**未提交的日志并不会应用到状态机，也不会被外部的客户端感知到**。

**要使得 `follower` 的日志集合跟自己保持完全一致，`leader` 必须先找到二者间最后一次达成一致的地方。因为一旦这条日志达成一致，在这之前的日志一定也都一致**。这个确认操作是**在 ==`AppendEntries RPC` 的一致性检查步骤完成的**。

> `leader` 如何同步数据到 `follower`？

**`Leader` 针对每个 `follower` 都维护一个 `next index`，表示下一条需要发送给该 `follower` 的日志索引==**。当一个 `leader`刚刚上任时，它**==初始化所有 `next index` 值为自己最后一条日志的 `index+1`==**。但凡某个 `follower` 的日志跟 `leader` 不一致，那么下次 `AppendEntries RPC` 的一致性检查就会失败。**在被 `follower` 拒绝这次 `Append Entries RPC` 后，`leader` 会减少 `next index` 的值并进行重试**。

**最终一定会存在一个 `next index` 使得 `leader` 和 `follower` 在这之前的日志都保持一致**。极端情况下 `next index` 为 `0`，表示 `follower` 没有任何日志与 `leader` 一致，`leader `必须从第一条日志开始同步。

针对每个 `follower`，一旦确定了 `next index` 的值，`leader `便开始从该 `index` 同步日志，`follower` 会删除掉现存的不一致的日志，保留 `leader` 最新同步过来的。

整个集群的日志会在这个简单的机制下**自动趋于一致**。此外要注意，**`leader` 从来不会覆盖或者删除自己的日志，而是强制 `follower` 与它保持一致**。	

## 安全性及正确性

> 上述"选主"和"日志复制"存在的问题

前面的章节我们讲述了 Raft 算法是如何选主和复制日志的，然而到目前为止我们描述的**这套机制还不能保证每个节点的状态机会严格按照相同的顺序 apply 日志**。想象以下场景：

1. Leader 将一些日志复制到了大多数节点上，进行 commit 后发生了宕机。
2. 某个 follower 并没有被复制到这些日志，但它参与选举并当选了下一任 leader。
3. 新的 leader 又同步并 commit 了一些日志，这些日志覆盖掉了其它节点上的上一任 committed 日志。
4. 各个节点的状态机可能 apply 了不同的日志序列，出现了不一致的情况。

因此我们需要对“选主+日志复制”这套机制加上一些额外的限制，来保证**状态机的安全性**，也就是 `Raft` 算法的正确性

### 对选举的限制

**`Candidate` 必须有足够的资格才能当选集群 `leader`**，否则它就会给集群带来不可预料的错误。`Candidate` 是否具备这个资格可以在选举时添加一个小小的条件来判断，即：

- **每个 `candidate` 必须在 `RequestVote RPC` 中携带自己本地日志的最新 `(term, index)`**，如果 **`follower` 发现这个 `candidate` 的日志还没有自己的新，则拒绝投票给该 `candidate`**；
- `candidate` 想要赢得选举成为 `leader`，必须得到集群**大多数节点**的投票，那么**它的日志就一定至少不落后于大多数节点**。又因为一条日志只有复制到了大多数节点才能被 `commit`，因此**能赢得选举的 `candidate` 一定拥有所有 `committed` 日志**；

### 对提交的限制

> 什么是 `commit`?

当 **`leader` 得知某条日志被集群过半的节点复制成功**时，就**可以进行 `commit`**，`committed` 日志**一定最终会被状态机应用**。

**所谓 `commit` 其实就是对日志简单进行一个标记**，表明其可以被应用到状态机，并针对相应的客户端请求进行响应。然而 **`leader` 并不能在任何时候都随意 `commit` 旧任期留下的日志**，即使它已经被复制到了大多数节点。

> 例子

![e41bf2c6317557dacec5683b0c936fb](https://user-images.githubusercontent.com/83362909/200125309-9358317c-b93c-4a38-b2de-81581dc0a38f.jpg)


上图从左到右按时间顺序模拟了问题场景。

**阶段a**：S1 是 leader，收到请求后将 (term2, index2) 只复制给了 S2，尚未复制给 S3 ~ S5。

**阶段b**：S1 宕机，S5 当选 term3 的 leader（S3、S4、S5 三票），收到请求后保存了 (term3, index2)，尚未复制给任何节点。

**阶段c**：S5 宕机，S1 恢复，S1 重新当选 term4 的 leader，继续将 (term2, index2) 复制给了 S3，已经满足大多数节点，我们将其 commit。

**阶段d**：S1 又宕机，S5 恢复，S5 重新当选 leader（S2、S3、S4 三票），将 (term3, inde2) 复制给了所有节点并 commit。注意，此时发生了致命错误，已经 committed 的 (term2, index2) 被 (term3, index2) 覆盖了。

为了避免这种错误，我们需要添加一个额外的限制：**==`Leader` 只允许 `commit` 包含当前 `term` 的日志==**。

## 集群成员变更

> 实际落地存在的问题

- **集群成员变更**：如何安全地改变集群的节点成员；
- **日志压缩**：如何解决日志集合无限制增长带来的问题；

### 直接两阶段切换

`Raft` 论文中给出了一种**无需停机的**、**自动化**的改变集群成员的方式，其实本质上还是**利用了 `Raft` 的核心算法**，**将集群成员配置作为一个特殊日志从 `leader` 节点同步到其它节点去**。

> "脑裂"现象【脑裂就是出现了先后或同时多个领导】

所有将集群**从旧配置直接完全切换到新配置**的方案都是**不安全**的。不可能让集群中的全部节点在“**同一时刻**”**原子地**切换其集群成员配置，所以在切换期间不同的节点看到的集群视图可能存在不同，最终可能导致集群存在多个 `leader`，这就"**脑裂**"现象。

一般脑裂情况都可以自行恢复，但是**==要求集群内的节点对其他所有的节点都有感知==**。发生多 `leader` 主要原因在于**各个节点对于集群节点数量感知存在差异**。

> 以下的极端脑裂情况无法恢复，必须人为引导

举例：假设现在集群有 `A` 、`B` 、`C` 三个节点，`C` 为领导，此时 `D` 和 `E` 加入，领导收到后，**没来得及同步集群拓展的节点配置到原集群的节点**，然后就**出现集群分化**，导致新加入的节点作为一个集群，原集群作为一个集群，然后**各自选主，引发集群内多个领导共存的情况**。

### 两阶段切换

`Raft` 使用一种两阶段方法平滑切换集群成员配置来避免遇到直接切换问题：

> 一阶段

- 客户端将 `C-new` 发送给 `leader`，`leader` 将 `C-old` 与 `C-new` 取**并集**并立即应用，我们表示为 **`C-old,new`**；
- `Leader` 将 `C-old,new` 包装为日志同步给其它节点；
- `Follower` 收到 `C-old,new` 后立即应用，当 **`C-old,new` 的大多数节点（即 `C-old` 的大多数节点和 `C-new` 的大多数节点）**都切换后，`leader` 将该日志 `commit`；

> 二阶段

- `Leader` 接着将 `C-new` 包装为日志同步给其它节点；
- `Follower` 收到`C-new` 后立即应用，如果此时发现自己不在 `C-new` 列表，则主动退出集群；
- `Leader` 确认 **`C-new` 的大多数节点**都切换成功后，给客户端发送执行成功的响应；

## 日志压缩

> 为何需要日志压缩？

`Raft` 核心算法维护了日志的一致性，通过应用日志就得到了一致的状态机，客户端的操作命令会被包装成日志交给 `Raft` 处理。然而在实际系统中，客户端操作是连绵不断的，但**日志却不能无限增长**，首先它会**占用很高的存储空间**，其次**每次系统重启时都需要完整回放一遍所有日志才能得到最新的状态机**。

**快照**（**`Snapshot`**）是一种常用的、简单的日志压缩方式，`ZooKeeper`、`Chubby` 等系统都在用。简单来说，就是将某一时刻系统的状态 `dump` 下来并落地存储，这样**该时刻之前的所有日志就都可以丢弃了**。

注意，**在 `Raft` 中我们只能为 `committed` 日志做 `snapshot`**，因为只有 `committed` 日志才是确保最终会应用到状态机的。

> 日志快照格式

![3b8a730e16e200ecd7ed328eef4ac48](https://user-images.githubusercontent.com/83362909/200126273-b9a2a9ce-6eaa-4231-8db9-4356c358789d.jpg)


- **日志的元数据**：最后一条被该快照 `apply` 的日志 `term` 及 `index`；
- **状态机**：前边全部日志 `apply` 后最终得到的状态机；

当 `leader` 需要给某个 `follower` 同步一些旧日志，但**这些日志已经被 `leader` 做了快照并删除掉了时，`leader` 就需要把该快照发送给 `follower`**。

同样，当集群中有新节点加入，或者某个节点宕机太久落后了太多日志时，**`leader` 也可以直接发送快照**，大量节约日志传输和回放时间。

## 注意
对于上一个任期的日志，必须等到本任期的leader新日志提交才能一起提交，防止出现已提交日志被覆盖的情况。
![image](https://user-images.githubusercontent.com/83362909/201023095-f8f44496-41b0-4c93-b1f5-08847cc5aefd.png)

