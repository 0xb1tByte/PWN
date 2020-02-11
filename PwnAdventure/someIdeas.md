**// moving to the secret island ?** </br>
virtual bool MoveToRandomLocationInRadius(float); </br>

**// changing the actor ?** </br>
    virtual bool MoveToActor(IActor *); </br>

**// show the inventory for another player ?** </br>
    virtual bool ShowInInventory(); </br>

**// printing these things in the message box ?** </br>
    virtual ILocalPlayer * GetLocalPlayer() const; </br>
    virtual const char * GetPlayerName(); </br>
    virtual const char * GetTeamName(); </br>

**// setting the quest? and solving all challenges?** </br>
    virtual void SetCurrentQuest(IQuest *); </br>
    virtual bool IsQuestCompleted(IQuest *); </br>
    virtual void MarkAsAchieved(IAchievement *); </br>
    virtual bool HasAchieved(IAchievement *); </br>

**// filling up the inventory?** </br>
    virtual bool AddItem(IItem *, uint32_t, bool); </br>
    bool PerformAddItem(IItem *, uint32_t, bool); </br>
    virtual void BuyItem(IActor *, IItem *, uint32_t); </br>
    virtual void SellItem(IActor *, IItem *, uint32_t); </br>

**// what these functions are doing?** </br>
    bool IsAdmin() const; </br>
    void DestroyActor(Actor *); </br>
    bool SpawnActor(Actor *, const Vector3 &, const Rotation &); </br>
    void RemoveAllActorsExceptPlayer(Player *); </br>

**// moving to another location?** </br>
    virtual void Teleport(const char *); </br>
    void PerformTeleport(const std::string &);</br> 
    virtual void FastTravel(const char *, const char *);</br>
    void PerformFastTravel(const std::string &, const std::string &); </br>
